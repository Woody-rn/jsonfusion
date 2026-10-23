import {
    Flex,
    Input,
    NativeSelect,
    Table,
} from "@chakra-ui/react";
import { Tooltip } from "../ui/tooltip";
import type { FieldInfo, FieldRole, PriorityMode } from "../../api/types";

interface FieldRowProps {
    field: FieldInfo;
    onChange: (patch: Partial<FieldInfo>) => void;
}

const ROLES: FieldRole[] = ["ANCHOR", "PRIORITY_F1", "PRIORITY_F2", "DELETE"];

const ROLE_HINTS: Record<FieldRole, string> = {
    ANCHOR: "Якорь. По этому полю ищутся совпадения. Ровно одно поле.",
    PRIORITY_F1: "Берём значение из первого файла.",
    PRIORITY_F2: "Берём значение из второго файла.",
    DELETE: "Удалить поле из результата.",
};

export default function FieldRow({ field, onChange }: FieldRowProps) {
    const isPriority =
        field.role === "PRIORITY_F1" || field.role === "PRIORITY_F2";

    return (
        <Table.Row>
            <Table.Cell fontWeight="medium">{field.name}</Table.Cell>
            <Table.Cell>{field.presentInFile1 ? "✓" : "—"}</Table.Cell>
            <Table.Cell>{field.presentInFile2 ? "✓" : "—"}</Table.Cell>

            <Table.Cell>
                <Tooltip content={ROLE_HINTS[field.role]}>
                    <NativeSelect.Root size="sm">
                        <NativeSelect.Field
                            value={field.role}
                            onChange={(e) =>
                                onChange({ role: e.target.value as FieldRole })
                            }
                        >
                            {ROLES.map((r) => (
                                <option key={r} value={r}>
                                    {r}
                                </option>
                            ))}
                        </NativeSelect.Field>
                        <NativeSelect.Indicator />
                    </NativeSelect.Root>
                </Tooltip>
            </Table.Cell>

            <Table.Cell>
                {isPriority ? (
                    <Flex gap="2" align="center">
                        <Tooltip content="Всегда перезаписывать или только если значение равно указанному.">
                            <NativeSelect.Root size="sm" width="120px">
                                <NativeSelect.Field
                                    value={field.priorityMode ?? "ALWAYS"}
                                    onChange={(e) =>
                                        onChange({
                                            priorityMode: e.target.value as PriorityMode,
                                        })
                                    }
                                >
                                    <option value="ALWAYS">ALWAYS</option>
                                    <option value="IF_EQUALS">IF_EQUALS</option>
                                </NativeSelect.Field>
                                <NativeSelect.Indicator />
                            </NativeSelect.Root>
                        </Tooltip>

                        {field.priorityMode === "IF_EQUALS" && (
                            <>
                                <Tooltip content="Перезаписываем только если значение во втором файле равно указанному. Пусто = null / пустая строка.">
                                    <Input
                                        size="sm"
                                        placeholder="значение"
                                        value={field.ifEqualsValue ?? ""}
                                        onChange={(e) =>
                                            onChange({ ifEqualsValue: e.target.value })
                                        }
                                        width="120px"
                                    />
                                </Tooltip>
                                <label>
                                    <input
                                        type="checkbox"
                                        checked={field.ifEqualsNull}
                                        onChange={(e) =>
                                            onChange({ ifEqualsNull: e.target.checked })
                                        }
                                    />{" "}
                                    null
                                </label>
                            </>
                        )}
                    </Flex>
                ) : (
                    <span>—</span>
                )}
            </Table.Cell>
        </Table.Row>
    );
}