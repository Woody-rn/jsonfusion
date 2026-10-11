import { Box, Flex, IconButton, Input, Text } from "@chakra-ui/react";
import type { UnmatchedRecordDto } from "../../api/types";

interface UnmatchedRowProps {
    record: UnmatchedRecordDto;
    anchorField: string;
    label: string | null;
    ignored: boolean;
    onLabelChange: (label: string) => void;
    onToggleIgnored: () => void;
}

const TYPE_LABELS: Record<string, string> = {
    ONLY_IN_LEFT: "no pair",
    ONLY_IN_RIGHT: "no pair",
    DUPLICATE: "duplicate",
};

export default function UnmatchedRow({
                                         record,
                                         anchorField,
                                         label,
                                         ignored,
                                         onLabelChange,
                                         onToggleIgnored,
                                     }: UnmatchedRowProps) {
    const anchorValue = record.record[anchorField];
    const displayValue = anchorValue == null ? "(no value)" : String(anchorValue);

    return (
        <Flex
            align="center"
            gap="2"
            p="2"
            borderBottomWidth="1px"
            opacity={ignored ? 0.4 : 1}
            bg={record.type === "DUPLICATE" ? "yellow.50" : "white"}
        >
            <Text fontSize="xs" color="gray.500" width="32px">
                {record.id}
            </Text>

            <Box flex="1" minW="0">
                <Text fontSize="sm" truncate title={displayValue}>
                    {displayValue}
                </Text>
                <Text fontSize="xs" color="gray.400">
                    {TYPE_LABELS[record.type]}
                </Text>
            </Box>

            <Input
                size="xs"
                width="50px"
                textAlign="center"
                placeholder="—"
                value={label ?? ""}
                onChange={(e) => onLabelChange(e.target.value)}
                disabled={ignored}
            />

            <IconButton
                aria-label={ignored ? "Unignore" : "Ignore"}
                size="xs"
                variant={ignored ? "solid" : "ghost"}
                colorPalette={ignored ? "red" : "gray"}
                onClick={onToggleIgnored}
            >
                ✕
            </IconButton>
        </Flex>
    );
}