import { Box, Button, Flex, Heading, Spinner, Text, VStack } from "@chakra-ui/react";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getFields, updateConfig } from "../api/config";
import { runMerge } from "../api/merge";
import type { UpdateConfigRequest } from "../api/types";
import AnchorSettingsPanel from "../components/fields/AnchorSettingsPanel";
import FieldsTable from "../components/fields/FieldsTable";
import NewFieldsPanel from "../components/fields/NewFieldsPanel";
import { useFieldsStore } from "../store/fieldsStore";
import { useSessionStore } from "../store/sessionStore";

export default function FieldsConfigPage() {
    const navigate = useNavigate();
    const sessionId = useSessionStore((s) => s.sessionId);

    const {
        fields,
        anchorSettings,
        newFields,
        file1Name,
        file2Name,
        loaded,
        setData,
        updateField,
        setComparison,
    } = useFieldsStore();

    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!sessionId || loaded) return;

        setLoading(true);
        getFields(sessionId)
            .then((data) => setData(data.fields, data.anchorSettings, data.newFields))
            .catch((e) => setError(e instanceof Error ? e.message : "Failed to load fields"))
            .finally(() => setLoading(false));
    }, [sessionId, loaded, setData]);

    const anchorCount = fields.filter((f) => f.role === "ANCHOR").length;
    const anchorValid = anchorCount === 1;
    const canSave = anchorValid && !saving;

    const handleSave = async () => {
        if (!sessionId || !anchorSettings) return;

        setSaving(true);
        setError(null);

        try {
            const request: UpdateConfigRequest = {
                fields: fields.map((f) => ({
                    name: f.name,
                    role: f.role,
                    priorityMode: f.priorityMode,
                    ifEqualsValue: f.ifEqualsValue,
                    ifEqualsNull: f.ifEqualsNull,
                })),
                anchorSettings,
                newFields,
            };

            const response = await updateConfig(sessionId, request);
            setData(response.fields, response.anchorSettings, response.newFields);
            await runMerge(sessionId);
            navigate("/unmatched");
        } catch (e) {
            setError(e instanceof Error ? e.message : "Save failed");
        } finally {
            setSaving(false);
        }
    };

    if (loading || !anchorSettings) {
        return (
            <Flex justify="center" py="16">
                <Spinner size="lg" />
            </Flex>
        );
    }

    return (
        <VStack align="stretch" gap="6">
            <Box>
                <Heading size="md">Configure fields</Heading>
                {file1Name && file2Name && (
                    <Text fontSize="sm" color="gray.500" mt="1">
                        {file1Name} ↔ {file2Name}
                    </Text>
                )}
            </Box>

            <AnchorSettingsPanel
                settings={anchorSettings}
                onChange={(patch) => {
                    setComparison(
                        patch.ignoreCase ?? anchorSettings.ignoreCase,
                        patch.ignoreExtraSpaces ?? anchorSettings.ignoreExtraSpaces
                    );
                }}
            />

            <FieldsTable fields={fields} onFieldChange={updateField} />

            <NewFieldsPanel
                newFields={newFields}
                onChange={(nf) => useFieldsStore.setState({ newFields: nf })}
            />

            {!anchorValid && (
                <Box bg="orange.50" color="orange.800" p="3" borderRadius="md">
                    Exactly one field must have role ANCHOR. Currently: {anchorCount}.
                </Box>
            )}

            {error && (
                <Box bg="red.50" color="red.700" p="3" borderRadius="md">
                    {error}
                </Box>
            )}

            <Flex justify="space-between" align="center">
                <Text fontSize="sm" color="gray.500">
                    Fields: {fields.length}, new: {newFields.length}
                </Text>
                <Button
                    colorPalette="blue"
                    onClick={handleSave}
                    disabled={!canSave}
                    loading={saving}
                >
                    Save & Continue
                </Button>
            </Flex>
        </VStack>
    );
}