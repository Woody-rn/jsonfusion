import { http } from "./client";
import type { MergeResponse, UnmatchedResponse, UpdateUnmatchedRequest } from "./types";

export async function runMerge(sessionId: string): Promise<MergeResponse> {
    const { data } = await http.post<MergeResponse>(`/sessions/${sessionId}/merge`);
    return data;
}

export async function getUnmatched(sessionId: string): Promise<UnmatchedResponse> {
    const { data } = await http.get<UnmatchedResponse>(`/sessions/${sessionId}/unmatched`);
    return data;
}

export async function updateUnmatchedDecisions(
    sessionId: string,
    request: UpdateUnmatchedRequest
): Promise<UnmatchedResponse> {
    const { data } = await http.put<UnmatchedResponse>(
        `/sessions/${sessionId}/unmatched/decisions`,
        request
    );
    return data;
}