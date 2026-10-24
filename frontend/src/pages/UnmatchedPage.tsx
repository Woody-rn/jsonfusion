import { Box, Button, Flex, Grid, Heading, Spinner, Text, VStack } from "@chakra-ui/react";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getUnmatched, updateUnmatchedDecisions } from "../api/merge";
import type { UpdateUnmatchedRequest } from "../api/types";
import UnmatchedList from "../components/unmatched/UnmatchedList";
import UnmatchedToolbar from "../components/unmatched/UnmatchedToolbar";
import { useFieldsStore } from "../store/fieldsStore";
import { useSessionStore } from "../store/sessionStore";
import { useUnmatchedStore } from "../store/unmatchedStore";

export default function UnmatchedPage() {
    const navigate = useNavigate();
    const sessionId = useSessionStore((s) => s.sessionId);
    const anchorField = useFieldsStore((s) => s.anchorSettings?.anchorField) ?? "name";

    const {
        unmatchedLeft,
        unmatchedRight,
        pairs,
        ignoredIds,
        loaded,
        setData,
        toggleIgnored,
        ignoreMany,
        clearIgnored,
        setLabel,
        unpairLeft,
        unpairRight,
    } = useUnmatchedStore();

    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [onlyDuplicates, setOnlyDuplicates] = useState(false);

    useEffect(() => {
        if (!sessionId || loaded) return;

        setLoading(true);
        getUnmatched(sessionId)
            .then((data) => {
                const pairsFromBackend = extractPairs(data.unmatchedLeft, data.unmatchedRight);
                const ignoredFromBackend = new Set<string>();
                data.unmatchedLeft.forEach((r) => r.ignored && ignoredFromBackend.add(r.id));
                data.unmatchedRight.forEach((r) => r.ignored && ignoredFromBackend.add(r.id));
                setData(data.unmatchedLeft, data.unmatchedRight, pairsFromBackend, ignoredFromBackend);
            })
            .catch((e) => setError(e instanceof Error ? e.message : "Failed to load"))
            .finally(() => setLoading(false));
    }, [sessionId, loaded, setData]);

    const filteredLeft = useMemo(
        () => (onlyDuplicates ? unmatchedLeft.filter((r) => r.type === "DUPLICATE") : unmatchedLeft),
        [unmatchedLeft, onlyDuplicates]
    );
    const filteredRight = useMemo(
        () => (onlyDuplicates ? unmatchedRight.filter((r) => r.type === "DUPLICATE") : unmatchedRight),
        [unmatchedRight, onlyDuplicates]
    );

    const totalCount = unmatchedLeft.length + unmatchedRight.length;

    const handleIgnoreVisible = () => {
        const ids = [
            ...filteredLeft.map((r) => r.id),
            ...filteredRight.map((r) => r.id),
        ];
        ignoreMany(ids);
    };

    const handleLabelChange = (id: string, label: string, side: "left" | "right") => {
        const trimmed = label.trim();
        if (!trimmed) {
            if (side === "left") unpairLeft(id);
            else unpairRight(id);
            return;
        }
        setLabel(id, trimmed, side);
    };

    const handleSave = async () => {
        if (!sessionId) return;
        setSaving(true);
        setError(null);

        try {
            // Отправляем только полные пары
            const completePairs = pairs.filter((p) => p.leftId && p.rightId);

            const request: UpdateUnmatchedRequest = {
                pairs: completePairs.map((p) => ({
                    leftId: p.leftId,
                    rightId: p.rightId,
                    label: p.label,
                })),
                ignoredIds: Array.from(ignoredIds),
            };

            const response = await updateUnmatchedDecisions(sessionId, request);
            const pairsFromBackend = extractPairs(response.unmatchedLeft, response.unmatchedRight);
            const ignoredFromBackend = new Set<string>();
            response.unmatchedLeft.forEach((r) => r.ignored && ignoredFromBackend.add(r.id));
            response.unmatchedRight.forEach((r) => r.ignored && ignoredFromBackend.add(r.id));
            setData(response.unmatchedLeft, response.unmatchedRight, pairsFromBackend, ignoredFromBackend);
            navigate("/result");
        } catch (e) {
            setError(e instanceof Error ? e.message : "Save failed");
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <Flex justify="center" py="16">
                <Spinner size="lg" />
            </Flex>
        );
    }

    return (
        <VStack align="stretch" gap="4">
            <Heading size="md">Unmatched records</Heading>

            <UnmatchedToolbar
                totalCount={totalCount}
                ignoredCount={ignoredIds.size}
                pairsCount={pairs.filter((p) => p.leftId && p.rightId).length}
                onlyDuplicates={onlyDuplicates}
                onOnlyDuplicatesChange={setOnlyDuplicates}
                onIgnoreAllVisible={handleIgnoreVisible}
                onClearIgnores={clearIgnored}
            />

            <Grid templateColumns={{ base: "1fr", md: "1fr 1fr" }} gap="4">
                <UnmatchedList
                    title="From file 1"
                    side="left"
                    records={filteredLeft}
                    anchorField={anchorField}
                    pairs={pairs}
                    ignoredIds={ignoredIds}
                    onLabelChange={handleLabelChange}
                    onToggleIgnored={toggleIgnored}
                />
                <UnmatchedList
                    title="From file 2"
                    side="right"
                    records={filteredRight}
                    anchorField={anchorField}
                    pairs={pairs}
                    ignoredIds={ignoredIds}
                    onLabelChange={handleLabelChange}
                    onToggleIgnored={toggleIgnored}
                />
            </Grid>

            {error && (
                <Box bg="red.50" color="red.700" p="3" borderRadius="md">
                    {error}
                </Box>
            )}

            <Flex justify="space-between" align="center">
                <Text fontSize="sm" color="gray.500">
                    Label matching records with the same number to pair them.
                </Text>
                <Button colorPalette="blue" onClick={handleSave} loading={saving}>
                    Save & Continue
                </Button>
            </Flex>
        </VStack>
    );
}

function extractPairs(
    left: { id: string; label: string | null }[],
    right: { id: string; label: string | null }[]
) {
    const rightByLabel = new Map<string, string>();
    right.forEach((r) => {
        if (r.label) rightByLabel.set(r.label, r.id);
    });

    const pairs: { leftId: string; rightId: string; label: string }[] = [];
    left.forEach((l) => {
        if (!l.label) return;
        const rightId = rightByLabel.get(l.label);
        if (rightId) {
            pairs.push({ leftId: l.id, rightId, label: l.label });
        }
    });
    return pairs;
}