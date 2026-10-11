import { Box, Checkbox, Flex, Text } from "@chakra-ui/react";
import type { AnchorSettings } from "../../api/types";

interface AnchorSettingsPanelProps {
    settings: AnchorSettings;
    onChange: (patch: Partial<AnchorSettings>) => void;
}

export default function AnchorSettingsPanel({ settings, onChange }: AnchorSettingsPanelProps) {
    return (
        <Box borderWidth="1px" borderRadius="md" p="4" bg="white">
            <Text fontWeight="bold" mb="3">Anchor comparison</Text>

            <Flex gap="6" wrap="wrap" align="center">
                <Checkbox.Root
                    checked={settings.ignoreCase}
                    onCheckedChange={(e) => onChange({ ignoreCase: !!e.checked })}
                >
                    <Checkbox.HiddenInput />
                    <Checkbox.Control />
                    <Checkbox.Label>Ignore case</Checkbox.Label>
                </Checkbox.Root>

                <Checkbox.Root
                    checked={settings.ignoreExtraSpaces}
                    onCheckedChange={(e) => onChange({ ignoreExtraSpaces: !!e.checked })}
                >
                    <Checkbox.HiddenInput />
                    <Checkbox.Control />
                    <Checkbox.Label>Ignore extra spaces</Checkbox.Label>
                </Checkbox.Root>
            </Flex>
        </Box>
    );
}