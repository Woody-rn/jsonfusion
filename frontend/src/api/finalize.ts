import { http } from "./client";
import { API_BASE_URL } from "./env";
import type { FinalizeResponse } from "./types";

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
    return `${API_BASE_URL}/api/sessions/${sessionId}/result`;
}

export function downloadUnmatchedUrl(sessionId: string): string {
    return `${API_BASE_URL}/api/sessions/${sessionId}/result/unmatched`;
}