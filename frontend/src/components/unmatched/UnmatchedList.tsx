import { Box, Heading, Text, VStack } from "@chakra-ui/react";
import type { UnmatchedRecordDto } from "../../api/types";
import type { UnmatchedPairState } from "../../store/unmatchedStore";
import UnmatchedRow from "./UnmatchedRow";

interface UnmatchedListProps {
    title: string;
    records: UnmatchedRecordDto[];
    side: "left" | "right";
    anchorField: string;
    pairs: UnmatchedPairState[];
    ignoredIds: Set<string>;
    onLabelChange: (id: string, label: string, side: "left" | "right") => void;
    onToggleIgnored: (id: string) => void;
}

export default function UnmatchedList({
                                          title,
                                          records,
                                          side,
                                          anchorField,
                                          pairs,
                                          ignoredIds,
                                          onLabelChange,
                                          onToggleIgnored,
                                      }: UnmatchedListProps) {
    const getLabelFor = (id: string): string | null => {
        const pair = pairs.find((p) =>
            side === "left" ? p.leftId === id : p.rightId === id
        );
        return pair?.label ?? null;
    };

    return (
        <Box borderWidth="1px" borderRadius="md" bg="white" overflow="hidden">
            <Box bg="gray.50" px="3" py="2" borderBottomWidth="1px">
                <Heading size="sm">{title}</Heading>
                <Text fontSize="xs" color="gray.500">{records.length} records</Text>
            </Box>

            <VStack align="stretch" gap="0" maxH="600px" overflowY="auto">
                {records.length === 0 ? (
                    <Text p="4" color="gray.500" fontSize="sm">No unmatched records</Text>
                ) : (
                    records.map((record) => (
                        <UnmatchedRow
                            key={record.id}
                            record={record}
                            anchorField={anchorField}
                            label={getLabelFor(record.id)}
                            ignored={ignoredIds.has(record.id)}
                            onLabelChange={(label) => onLabelChange(record.id, label, side)}
                            onToggleIgnored={() => onToggleIgnored(record.id)}
                        />
                    ))
                )}
            </VStack>
        </Box>
    );
}