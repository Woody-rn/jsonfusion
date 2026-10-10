import { create } from "zustand";
import type { AnchorSettings, FieldInfo, NewField } from "../api/types";

interface FieldsStore {
    fields: FieldInfo[];
    anchorSettings: AnchorSettings | null;
    newFields: NewField[];
    loaded: boolean;
    setData: (fields: FieldInfo[], anchorSettings: AnchorSettings, newFields: NewField[]) => void;
    updateField: (name: string, patch: Partial<FieldInfo>) => void;
    setAnchorField: (name: string) => void;
    setComparison: (ignoreCase: boolean, ignoreExtraSpaces: boolean) => void;
    clear: () => void;
}

export const useFieldsStore = create<FieldsStore>((set) => ({
    fields: [],
    anchorSettings: null,
    newFields: [],
    loaded: false,

    setData: (fields, anchorSettings, newFields) =>
        set({ fields, anchorSettings, newFields, loaded: true }),

    updateField: (name, patch) =>
        set((state) => ({
            fields: state.fields.map((f) => (f.name === name ? { ...f, ...patch } : f)),
        })),

    setAnchorField: (name) =>
        set((state) => ({
            anchorSettings: state.anchorSettings
                ? { ...state.anchorSettings, anchorField: name }
                : null,
        })),

    setComparison: (ignoreCase, ignoreExtraSpaces) =>
        set((state) => ({
            anchorSettings: state.anchorSettings
                ? { ...state.anchorSettings, ignoreCase, ignoreExtraSpaces }
                : null,
        })),

    clear: () => set({ fields: [], anchorSettings: null, newFields: [], loaded: false }),
}));