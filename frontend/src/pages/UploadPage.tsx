import { Box, Button, Flex, Heading, Text, VStack } from "@chakra-ui/react";
import { isAxiosError } from "axios";
import { useCallback, useState } from "react";
import { useDropzone } from "react-dropzone";
import { useNavigate } from "react-router-dom";
import { uploadFiles } from "../api/files";
import { createSession, deleteSession, getSession } from "../api/sessions";
import { useFieldsStore } from "../store/fieldsStore";
import { useSessionStore } from "../store/sessionStore";
import { useUnmatchedStore } from "../store/unmatchedStore";

interface FileSlotProps {
    label: string;
    file: File | null;
    onFile: (file: File) => void;
}

function FileSlot({ label, file, onFile }: FileSlotProps) {
    const onDrop = useCallback(
        (accepted: File[]) => {
            if (accepted.length > 0) onFile(accepted[0]);
        },
        [onFile]
    );

    const { getRootProps, getInputProps, isDragActive } = useDropzone({
        onDrop,
        accept: { "application/json": [".json"] },
        multiple: false,
    });

    return (
        <Box
            {...getRootProps()}
            borderWidth="2px"
            borderStyle="dashed"
            borderColor={isDragActive ? "blue.500" : "gray.300"}
            borderRadius="md"
            p="8"
            textAlign="center"
            cursor="pointer"
            bg={isDragActive ? "blue.50" : "white"}
            _hover={{ borderColor: "blue.400" }}
        >
            <input {...getInputProps()} />
            <Text fontWeight="bold" mb="2">{label}</Text>
            {file ? (
                <Text color="green.600">{file.name}</Text>
            ) : (
                <Text color="gray.500">
                    {isDragActive ? "Drop file here..." : "Drag & drop JSON here, or click to select"}
                </Text>
            )}
        </Box>
    );
}

export default function UploadPage() {
    const navigate = useNavigate();
    const { sessionId, setSessionId, clear } = useSessionStore();
    const setFileNames = useFieldsStore((s) => s.setFileNames);
    const clearFields = useFieldsStore((s) => s.clear);
    const clearUnmatched = useUnmatchedStore((s) => s.clear);

    const [file1, setFile1] = useState<File | null>(null);
    const [file2, setFile2] = useState<File | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const canUpload = file1 !== null && file2 !== null && !loading;

    const resetAll = () => {
        clear();
        clearFields();
        clearUnmatched();
    };

    const ensureFreshSession = async (): Promise<string> => {
        if (sessionId) {
            try {
                await getSession(sessionId);
                return sessionId;
            } catch {
                // session gone — reset
                resetAll();
            }
        }
        const session = await createSession();
        setSessionId(session.sessionId);
        return session.sessionId;
    };

    const handleUpload = async () => {
        if (!file1 || !file2) return;

        setLoading(true);
        setError(null);

        try {
            let currentSessionId = await ensureFreshSession();

            try {
                await uploadFiles(currentSessionId, file1, file2);
            } catch (err) {
                if (isAxiosError(err) && (err.response?.status === 409 || err.response?.status === 404)) {
                    await deleteSession(currentSessionId).catch(() => undefined);
                    resetAll();
                    const fresh = await createSession();
                    currentSessionId = fresh.sessionId;
                    setSessionId(currentSessionId);
                    await uploadFiles(currentSessionId, file1, file2);
                } else {
                    throw err;
                }
            }

            setFileNames(file1.name, file2.name);
            navigate("/fields");
        } catch (e) {
            setError(e instanceof Error ? e.message : "Upload failed");
        } finally {
            setLoading(false);
        }
    };

    return (
        <VStack align="stretch" gap="6">
            <Heading size="md">Upload two JSON files</Heading>

            <Flex gap="4" direction={{ base: "column", md: "row" }}>
                <Box flex="1">
                    <FileSlot label="File 1" file={file1} onFile={setFile1} />
                </Box>
                <Box flex="1">
                    <FileSlot label="File 2" file={file2} onFile={setFile2} />
                </Box>
            </Flex>

            {error && (
                <Box bg="red.50" color="red.700" p="3" borderRadius="md">
                    {error}
                </Box>
            )}

            <Flex justify="flex-end">
                <Button
                    colorPalette="blue"
                    onClick={handleUpload}
                    disabled={!canUpload}
                    loading={loading}
                >
                    Upload & Continue
                </Button>
            </Flex>
        </VStack>
    );
}