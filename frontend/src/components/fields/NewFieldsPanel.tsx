import { Box, Button, Flex, IconButton, Input, Text } from "@chakra-ui/react";
import { useState } from "react";
import type { NewField } from "../../api/types";

interface NewFieldsPanelProps {
    newFields: NewField[];
    onChange: (fields: NewField[]) => void;
}

export default function NewFieldsPanel({ newFields, onChange }: NewFieldsPanelProps) {
    const [name, setName] = useState("");
    const [value, setValue] = useState("");

    const add = () => {
        const trimmed = name.trim();
        if (!trimmed) return;
        onChange([...newFields, { name: trimmed, defaultValue: value }]);
        setName("");
        setValue("");
    };

    const remove = (index: number) => {
        onChange(newFields.filter((_, i) => i !== index));
    };

    return (
        <Box borderWidth="1px" borderRadius="md" p="4" bg="white">
            <Text fontWeight="bold" mb="3">Add new fields</Text>

            {newFields.length > 0 && (
                <Box mb="3">
                    {newFields.map((f, i) => (
                        <Flex key={i} align="center" gap="2" mb="1">
                            <Text fontWeight="medium">{f.name}</Text>
                            <Text color="gray.500">= {JSON.stringify(f.defaultValue)}</Text>
                            <IconButton
                                aria-label="Remove"
                                size="xs"
                                variant="ghost"
                                onClick={() => remove(i)}
                            >
                                ✕
                            </IconButton>
                        </Flex>
                    ))}
                </Box>
            )}

            <Flex gap="2" wrap="wrap">
                <Input
                    size="sm"
                    placeholder="field name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    width="200px"
                />
                <Input
                    size="sm"
                    placeholder="default value"
                    value={value}
                    onChange={(e) => setValue(e.target.value)}
                    width="200px"
                />
                <Button size="sm" onClick={add} disabled={!name.trim()}>
                    Add
                </Button>
            </Flex>
        </Box>
    );
}