import {
    Box,
    Button,
    Checkbox,
    Flex,
    Heading,
    Link,
    Table,
    Text,
    VStack,
} from "@chakra-ui/react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { downloadResultUrl, downloadUnmatchedUrl, finalize } from "../api/finalize";
import type { FinalizeResponse } from "../api/types";
import { useSessionStore } from "../store/sessionStore";
import { useUnmatchedStore } from "../store/unmatchedStore";

export default function ResultPage() {
    const navigate = useNavigate();
    const sessionId = useSessionStore((s) => s.sessionId);
    const { mergeSummary, pairs, ignoredIds } = useUnmatchedStore();

    const [saveUnmatched, setSaveUnmatched] = useState(true);
    const [finalizing, setFinalizing] = useState(false);
    const [result, setResult] = useState<FinalizeResponse | null>(null);
    const [error, setError] = useState<string | null>(null);

    const handleFinalize = async () => {
        if (!sessionId) return;
        setFinalizing(true);
        setError(null);

        try {
            const response = await finalize(sessionId, saveUnmatched);
            setResult(response);
        } catch (e) {
            setError(e instanceof Error ? e.message : "Finalize failed");
        } finally {
            setFinalizing(false);
        }
    };

    return (
        <VStack align="stretch" gap="6">
            <Heading size="md">Result</Heading>

            <Box borderWidth="1px" borderRadius="md" p="4" bg="white">
                <Text fontWeight="bold" mb="3">Summary</Text>
                <Table.Root size="sm">
                    <Table.Body>
                        <Table.Row>
                            <Table.Cell>Matched (auto-merge)</Table.Cell>
                            <Table.Cell>{mergeSummary?.matchedCount ?? "—"}</Table.Cell>
                        </Table.Row>
                        <Table.Row>
                            <Table.Cell>Unmatched left</Table.Cell>
                            <Table.Cell>{mergeSummary?.unmatchedLeftCount ?? "—"}</Table.Cell>
                        </Table.Row>
                        <Table.Row>
                            <Table.Cell>Unmatched right</Table.Cell>
                            <Table.Cell>{mergeSummary?.unmatchedRightCount ?? "—"}</Table.Cell>
                        </Table.Row>
                        <Table.Row>
                            <Table.Cell>Manual pairs</Table.Cell>
                            <Table.Cell>{pairs.length}</Table.Cell>
                        </Table.Row>
                        <Table.Row>
                            <Table.Cell>Ignored</Table.Cell>
                            <Table.Cell>{ignoredIds.size}</Table.Cell>
                        </Table.Row>
                    </Table.Body>
                </Table.Root>
            </Box>

            {!result && (
                <Box borderWidth="1px" borderRadius="md" p="4" bg="white">
                    <Checkbox.Root
                        checked={saveUnmatched}
                        onCheckedChange={(e) => setSaveUnmatched(!!e.checked)}
                    >
                        <Checkbox.HiddenInput />
                        <Checkbox.Control />
                        <Checkbox.Label>
                            Save unresolved unmatched records to a separate file
                        </Checkbox.Label>
                    </Checkbox.Root>
                </Box>
            )}

            {error && (
                <Box bg="red.50" color="red.700" p="3" borderRadius="md">
                    {error}
                </Box>
            )}

            {!result ? (
                <Flex justify="flex-end">
                    <Button
                        colorPalette="blue"
                        onClick={handleFinalize}
                        loading={finalizing}
                    >
                        Finalize
                    </Button>
                </Flex>
            ) : (
                <Box borderWidth="1px" borderRadius="md" p="4" bg="white">
                    <Text fontWeight="bold" mb="3">Finalized</Text>
                    <Table.Root size="sm" mb="4">
                        <Table.Body>
                            <Table.Row>
                                <Table.Cell>Total records</Table.Cell>
                                <Table.Cell>{result.resultSummary.totalRecords}</Table.Cell>
                            </Table.Row>
                            <Table.Row>
                                <Table.Cell>From matched</Table.Cell>
                                <Table.Cell>{result.resultSummary.fromMatched}</Table.Cell>
                            </Table.Row>
                            <Table.Row>
                                <Table.Cell>From pairs</Table.Cell>
                                <Table.Cell>{result.resultSummary.fromPairs}</Table.Cell>
                            </Table.Row>
                            <Table.Row>
                                <Table.Cell>Unmatched saved</Table.Cell>
                                <Table.Cell>{result.resultSummary.unmatchedSaved}</Table.Cell>
                            </Table.Row>
                            <Table.Row>
                                <Table.Cell>Ignored</Table.Cell>
                                <Table.Cell>{result.resultSummary.ignoredCount}</Table.Cell>
                            </Table.Row>
                        </Table.Body>
                    </Table.Root>

                    <Flex gap="3">
                        <Link
                            href={downloadResultUrl(sessionId!)}
                            download="merged-result.json"
                        >
                            <Button colorPalette="blue">Download merged</Button>
                        </Link>

                        {result.unmatchedResultAvailable && (
                            <Link
                                href={downloadUnmatchedUrl(sessionId!)}
                                download="unmatched.json"
                            >
                                <Button variant="outline">Download unmatched</Button>
                            </Link>
                        )}

                        <Button variant="ghost" onClick={() => navigate("/upload")}>
                            Start over
                        </Button>
                    </Flex>
                </Box>
            )}
        </VStack>
    );
}