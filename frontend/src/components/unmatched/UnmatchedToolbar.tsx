import { Box, Button, Checkbox, Flex, Text } from "@chakra-ui/react";

interface UnmatchedToolbarProps {
    totalCount: number;
    ignoredCount: number;
    pairsCount: number;
    onlyDuplicates: boolean;
    onOnlyDuplicatesChange: (value: boolean) => void;
    onIgnoreAllVisible: () => void;
    onClearIgnores: () => void;
}

export default function UnmatchedToolbar({
                                             totalCount,
                                             ignoredCount,
                                             pairsCount,
                                             onlyDuplicates,
                                             onOnlyDuplicatesChange,
                                             onIgnoreAllVisible,
                                             onClearIgnores,
                                         }: UnmatchedToolbarProps) {
    return (
        <Box borderWidth="1px" borderRadius="md" p="3" bg="white">
            <Flex align="center" justify="space-between" gap="4" wrap="wrap">
                <Flex align="center" gap="4">
                    <Checkbox.Root
                        checked={onlyDuplicates}
                        onCheckedChange={(e) => onOnlyDuplicatesChange(!!e.checked)}
                    >
                        <Checkbox.HiddenInput />
                        <Checkbox.Control />
                        <Checkbox.Label>Only duplicates</Checkbox.Label>
                    </Checkbox.Root>

                    <Text fontSize="sm" color="gray.600">
                        Total: {totalCount} · Ignored: {ignoredCount} · Pairs: {pairsCount}
                    </Text>
                </Flex>

                <Flex gap="2">
                    <Button size="sm" variant="outline" onClick={onIgnoreAllVisible}>
                        Ignore visible
                    </Button>
                    <Button size="sm" variant="outline" onClick={onClearIgnores} disabled={ignoredCount === 0}>
                        Clear ignores
                    </Button>
                </Flex>
            </Flex>
        </Box>
    );
}