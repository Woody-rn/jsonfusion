import { Table } from "@chakra-ui/react";
import type { FieldInfo } from "../../api/types";
import FieldRow from "./FieldRow";

interface FieldsTableProps {
    fields: FieldInfo[];
    onFieldChange: (name: string, patch: Partial<FieldInfo>) => void;
}

export default function FieldsTable({ fields, onFieldChange }: FieldsTableProps) {
    return (
        <Table.Root size="sm" variant="outline" stickyHeader>
            <Table.Header>
                <Table.Row>
                    <Table.ColumnHeader>Field</Table.ColumnHeader>
                    <Table.ColumnHeader>F1</Table.ColumnHeader>
                    <Table.ColumnHeader>F2</Table.ColumnHeader>
                    <Table.ColumnHeader>Role</Table.ColumnHeader>
                    <Table.ColumnHeader>Condition</Table.ColumnHeader>
                </Table.Row>
            </Table.Header>
            <Table.Body>
                {fields.map((field) => (
                    <FieldRow
                        key={field.name}
                        field={field}
                        onChange={(patch) => onFieldChange(field.name, patch)}
                    />
                ))}
            </Table.Body>
        </Table.Root>
    );
}