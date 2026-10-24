import {create} from "zustand";
import type {MergeSummary, UnmatchedRecordDto} from "../api/types";

export interface UnmatchedPairState {
    leftId: string;
    rightId: string;
    label: string;
}

interface UnmatchedStore {
    unmatchedLeft: UnmatchedRecordDto[];
    unmatchedRight: UnmatchedRecordDto[];
    pairs: UnmatchedPairState[];
    ignoredIds: Set<string>;
    mergeSummary: MergeSummary | null;
    loaded: boolean;

    setData: (
        left: UnmatchedRecordDto[],
        right: UnmatchedRecordDto[],
        pairs: UnmatchedPairState[],
        ignored: Set<string>
    ) => void;
    setMergeSummary: (summary: MergeSummary) => void;
    toggleIgnored: (id: string) => void;
    ignoreMany: (ids: string[]) => void;
    ignoreAll: () => void;
    clearIgnored: () => void;
    setLabel: (id: string, label: string, side: "left" | "right") => void;
    unpairLeft: (leftId: string) => void;
    unpairRight: (rightId: string) => void;
    clear: () => void;
}

export const useUnmatchedStore = create<UnmatchedStore>((set) => ({
    unmatchedLeft: [],
    unmatchedRight: [],
    pairs: [],
    ignoredIds: new Set(),
    mergeSummary: null,
    loaded: false,

    setData: (left, right, pairs, ignored) =>
        set({
            unmatchedLeft: left,
            unmatchedRight: right,
            pairs,
            ignoredIds: new Set(ignored),
            loaded: true,
        }),

    setMergeSummary: (mergeSummary) => set({mergeSummary}),

    toggleIgnored: (id) =>
        set((state) => {
            const next = new Set(state.ignoredIds);
            if (next.has(id)) next.delete(id);
            else next.add(id);
            return {ignoredIds: next};
        }),

    ignoreMany: (ids) =>
        set((state) => {
            const next = new Set(state.ignoredIds);
            ids.forEach((id) => next.add(id));
            return {ignoredIds: next};
        }),

    ignoreAll: () =>
        set((state) => {
            const all = new Set<string>();
            state.unmatchedLeft.forEach((r) => all.add(r.id));
            state.unmatchedRight.forEach((r) => all.add(r.id));
            return {ignoredIds: all, pairs: []};
        }),

    clearIgnored: () => set({ignoredIds: new Set()}),

    setLabel: (id, label, side) =>
        set((state) => {
            // 1. Найти существующую пару с этим ID
            const existingIdx = state.pairs.findIndex((p) =>
                side === "left" ? p.leftId === id : p.rightId === id
            );

            const newPairs = [...state.pairs];

            if (existingIdx >= 0) {
                // Обновить метку существующей пары
                newPairs[existingIdx] = {...newPairs[existingIdx], label};
            } else {
                // Создать новую "черновую" пару (partner id = "")
                newPairs.push({
                    leftId: side === "left" ? id : "",
                    rightId: side === "right" ? id : "",
                    label,
                });
            }

            // 2. Попробовать слить черновые пары с одинаковыми метками
            const merged = mergeDrafts(newPairs);


            // 3. Убрать ID из игноров, если они попали в пару
            const touchedIds = new Set<string>();
            merged.forEach((p) => {
                if (p.leftId) touchedIds.add(p.leftId);
                if (p.rightId) touchedIds.add(p.rightId);
            });
            const ignoredIds = new Set(
                [...state.ignoredIds].filter((i) => !touchedIds.has(i))
            );

            return {pairs: merged, ignoredIds};
        }),

    unpairLeft: (leftId) =>
        set((state) => ({
            pairs: state.pairs.filter((p) => p.leftId !== leftId),
        })),

    unpairRight: (rightId) =>
        set((state) => ({
            pairs: state.pairs.filter((p) => p.rightId !== rightId),
        })),

    clear: () =>
        set({
            unmatchedLeft: [],
            unmatchedRight: [],
            pairs: [],
            ignoredIds: new Set(),
            mergeSummary: null,
            loaded: false,
        }),
}));

/**
 * Сливает черновые пары (где одна сторона пустая) с полными по совпадению label.
 */
function mergeDrafts(pairs: UnmatchedPairState[]): UnmatchedPairState[] {
    const byLabel = new Map<string, UnmatchedPairState[]>();
    for (const p of pairs) {
        const arr = byLabel.get(p.label) ?? [];
        arr.push(p);
        byLabel.set(p.label, arr);
    }

    const result: UnmatchedPairState[] = [];

    for (const group of byLabel.values()) {
        const lefts = group.filter((p) => p.leftId && !p.rightId);
        const rights = group.filter((p) => !p.leftId && p.rightId);
        const full = group.filter((p) => p.leftId && p.rightId);

        result.push(...full);

        // Пары из left-черновиков и right-черновиков
        const pairsCount = Math.min(lefts.length, rights.length);
        for (let i = 0; i < pairsCount; i++) {
            result.push({
                leftId: lefts[i].leftId,
                rightId: rights[i].rightId,
                label: lefts[i].label,
            });
        }

        // Оставшиеся черновики — сохраняем как есть
        for (let i = pairsCount; i < lefts.length; i++) result.push(lefts[i]);
        for (let i = pairsCount; i < rights.length; i++) result.push(rights[i]);
    }

    return result;
}