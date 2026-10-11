import { create } from "zustand";
import type { AnchorSettings, FieldInfo, NewField } from "../api/types";

interface FieldsStore {
    fields: FieldInfo[];
    anchorSettings: AnchorSettings | null;
    newFields: NewField[];
    file1Name: string | null;
    file2Name: string | null;
    loaded: boolean;
    setData: (fields: FieldInfo[], anchorSettings: AnchorSettings, newFields: NewField[]) => void;
    setFileNames: (file1Name: string, file2Name: string) => void;
    updateField: (name: string, patch: Partial<FieldInfo>) => void;
    setComparison: (ignoreCase: boolean, ignoreExtraSpaces: boolean) => void;
    clear: () => void;
}

export const useFieldsStore = create<FieldsStore>((set) => ({
    fields: [],
    anchorSettings: null,
    newFields: [],
    file1Name: null,
    file2Name: null,
    loaded: false,

    setData: (fields, anchorSettings, newFields) =>
        set({ fields, anchorSettings, newFields, loaded: true }),

    setFileNames: (file1Name, file2Name) => set({ file1Name, file2Name }),

    updateField: (name, patch) =>
        set((state) => {
            const updatedFields = state.fields.map((f) => {
                if (f.name === name) {
                    return { ...f, ...patch };
                }
                if (patch.role === "ANCHOR" && f.role === "ANCHOR") {
                    return {
                        ...f,
                        role: "PRIORITY_F1" as const,
                        priorityMode: "ALWAYS" as const,
                        ifEqualsValue: null,
                        ifEqualsNull: false,
                    };
                }
                return f;
            });

            let anchorField = state.anchorSettings?.anchorField ?? "name";
            if (patch.role === "ANCHOR") {
                anchorField = name;
            }

            return {
                fields: updatedFields,
                anchorSettings: state.anchorSettings
                    ? { ...state.anchorSettings, anchorField }
                    : null,
            };
        }),

    setComparison: (ignoreCase, ignoreExtraSpaces) =>
        set((state) => ({
            anchorSettings: state.anchorSettings
                ? { ...state.anchorSettings, ignoreCase, ignoreExtraSpaces }
                : null,
        })),

    clear: () =>
        set({
            fields: [],
            anchorSettings: null,
            newFields: [],
            file1Name: null,
            file2Name: null,
            loaded: false,
        }),
}));