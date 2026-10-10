export type SessionStatus =
    | "CREATED"
    | "FILES_UPLOADED"
    | "CONFIGURED"
    | "MERGED"
    | "UNMATCHED_RESOLVED"
    | "FINALIZED";

export interface SessionResponse {
    sessionId: string;
    status: SessionStatus;
    createdAt: string;
    lastAccessAt: string;
}

export interface FileAnalysis {
    recordsCount: number;
    fields: string[];
}

export interface UploadFilesResponse {
    sessionId: string;
    status: SessionStatus;
    file1: FileAnalysis;
    file2: FileAnalysis;
}

export type FieldRole = "ANCHOR" | "PRIORITY_F1" | "PRIORITY_F2" | "DELETE";
export type PriorityMode = "ALWAYS" | "IF_EQUALS";

export interface FieldInfo {
    name: string;
    presentInFile1: boolean;
    presentInFile2: boolean;
    defaultRole: FieldRole;
    role: FieldRole;
    priorityMode: PriorityMode | null;
    ifEqualsValue: string | null;
    ifEqualsNull: boolean;
}

export interface AnchorSettings {
    anchorField: string;
    ignoreCase: boolean;
    ignoreExtraSpaces: boolean;
}

export interface NewField {
    name: string;
    defaultValue: unknown;
}

export interface FieldsResponse {
    fields: FieldInfo[];
    anchorSettings: AnchorSettings;
    newFields: NewField[];
}

export interface FieldRuleDto {
    name: string;
    role: FieldRole;
    priorityMode: PriorityMode | null;
    ifEqualsValue: string | null;
    ifEqualsNull: boolean;
}

export interface UpdateConfigRequest {
    fields: FieldRuleDto[];
    anchorSettings: AnchorSettings;
    newFields: NewField[];
}

export interface MergeSummary {
    matchedCount: number;
    unmatchedLeftCount: number;
    unmatchedRightCount: number;
}

export interface MergeResponse {
    sessionId: string;
    status: SessionStatus;
    summary: MergeSummary;
}

export type UnmatchedType = "ONLY_IN_LEFT" | "ONLY_IN_RIGHT" | "DUPLICATE";

export interface UnmatchedRecordDto {
    id: string;
    record: Record<string, unknown>;
    type: UnmatchedType;
    label: string | null;
    ignored: boolean;
}

export interface UnmatchedResponse {
    unmatchedLeft: UnmatchedRecordDto[];
    unmatchedRight: UnmatchedRecordDto[];
}

export interface UnmatchedPairDto {
    leftId: string;
    rightId: string;
    label: string | null;
}

export interface UpdateUnmatchedRequest {
    pairs: UnmatchedPairDto[];
    ignoredIds: string[];
}

export interface ResultSummary {
    totalRecords: number;
    fromMatched: number;
    fromPairs: number;
    unmatchedSaved: number;
    ignoredCount: number;
}

export interface FinalizeResponse {
    sessionId: string;
    status: SessionStatus;
    resultSummary: ResultSummary;
    unmatchedResultAvailable: boolean;
}