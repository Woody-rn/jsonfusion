import { http } from "./client";
import type { FieldsResponse, UpdateConfigRequest } from "./types";

export async function getFields(sessionId: string): Promise<FieldsResponse> {
    const { data } = await http.get<FieldsResponse>(`/sessions/${sessionId}/fields`);
    return data;
}

export async function updateConfig(
    sessionId: string,
    request: UpdateConfigRequest
): Promise<FieldsResponse> {
    const { data } = await http.put<FieldsResponse>(
        `/sessions/${sessionId}/config`,
        request
    );
    return data;
}