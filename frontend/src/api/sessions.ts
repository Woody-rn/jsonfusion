import { http } from "./client";
import type { SessionResponse } from "./types";

export async function createSession(): Promise<SessionResponse> {
    const { data } = await http.post<SessionResponse>("/sessions");
    return data;
}

export async function getSession(sessionId: string): Promise<SessionResponse> {
    const { data } = await http.get<SessionResponse>(`/sessions/${sessionId}`);
    return data;
}

export async function deleteSession(sessionId: string): Promise<void> {
    await http.delete(`/sessions/${sessionId}`);
}