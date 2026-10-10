import {http} from "./client";
import type {UploadFilesResponse} from "./types";

export async function uploadFiles(
    sessionId: string,
    file1: File,
    file2: File
): Promise<UploadFilesResponse> {
    const form = new FormData();
    form.append("file1", file1);
    form.append("file2", file2);

    const {data} = await http.post<UploadFilesResponse>(
        `/sessions/${sessionId}/files`,
        form,
        {headers: {"Content-Type": "multipart/form-data"}}
    );
    return data;
}