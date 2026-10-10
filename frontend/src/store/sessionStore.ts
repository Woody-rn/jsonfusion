import { create } from "zustand";
import type { SessionStatus } from "../api/types";

interface SessionStore {
    sessionId: string | null;
    status: SessionStatus | null;
    setSessionId: (id: string) => void;
    setStatus: (status: SessionStatus) => void;
    clear: () => void;
}

export const useSessionStore = create<SessionStore>((set) => ({
    sessionId: null,
    status: null,
    setSessionId: (id) => {
        sessionStorage.setItem("sessionId", id);
        set({ sessionId: id });
    },
    setStatus: (status) => set({ status }),
    clear: () => {
        sessionStorage.removeItem("sessionId");
        set({ sessionId: null, status: null });
    },
}));