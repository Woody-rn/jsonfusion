import { create } from "zustand";
import type { UnmatchedRecordDto } from "../api/types";

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
    loaded: boolean;

    setData: (
        left: UnmatchedRecordDto[],
        right: UnmatchedRecordDto[],
        pairs: UnmatchedPairState[],
        ignored: Set<string>
    ) => void;
    toggleIgnored: (id: string) => void;
    ignoreAll: () => void;
    clearIgnored: () => void;
    pairRecords: (leftId: string, rightId: string, label: string) => void;
    unpairLeft: (leftId: string) => void;
    unpairRight: (rightId: string) => void;
    clear: () => void;
}

export const useUnmatchedStore = create<UnmatchedStore>((set) => ({
    unmatchedLeft: [],
    unmatchedRight: [],
    pairs: [],
    ignoredIds: new Set(),
    loaded: false,

    setData: (left, right, pairs, ignored) =>
        set({
            unmatchedLeft: left,
            unmatchedRight: right,
            pairs,
            ignoredIds: new Set(ignored),
            loaded: true,
        }),

    toggleIgnored: (id) =>
        set((state) => {
            const next = new Set(state.ignoredIds);
            if (next.has(id)) next.delete(id);
            else next.add(id);
            return { ignoredIds: next };
        }),

    ignoreAll: () =>
        set((state) => {
            const all = new Set<string>();
            state.unmatchedLeft.forEach((r) => all.add(r.id));
            state.unmatchedRight.forEach((r) => all.add(r.id));
            return { ignoredIds: all, pairs: [] };
        }),

    clearIgnored: () => set({ ignoredIds: new Set() }),

    pairRecords: (leftId, rightId, label) =>
        set((state) => {
            const pairs = state.pairs.filter(
                (p) => p.leftId !== leftId && p.rightId !== rightId
            );
            pairs.push({ leftId, rightId, label });
            const ignoredIds = new Set(state.ignoredIds);
            ignoredIds.delete(leftId);
            ignoredIds.delete(rightId);
            return { pairs, ignoredIds };
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
            loaded: false,
        }),
}));