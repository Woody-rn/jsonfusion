import { http } from "./client";
import type { FinalizeResponse } from "./types";

const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8089";

export async function finalize(
    sessionId: string,
    saveUnmatched: boolean
): Promise<FinalizeResponse> {
    const { data } = await http.post<FinalizeResponse>(
        `/sessions/${sessionId}/finalize`,
        { saveUnmatched }
    );
    return data;
}

export function downloadResultUrl(sessionId: string): string {
    return `${BASE_URL}/api/sessions/${sessionId}/result`;
}

export function downloadUnmatchedUrl(sessionId: string): string {
    return `${BASE_URL}/api/sessions/${sessionId}/result/unmatched`;
}