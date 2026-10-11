import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Table } from "@chakra-ui/react";
import { ChakraProvider, defaultSystem } from "@chakra-ui/react";
import { describe, expect, it, vi } from "vitest";
import type { FieldInfo } from "../../api/types";
import FieldRow from "./FieldRow";

function makeField(overrides: Partial<FieldInfo> = {}): FieldInfo {
    return {
        name: "name",
        presentInFile1: true,
        presentInFile2: true,
        defaultRole: "ANCHOR",
        role: "ANCHOR",
        priorityMode: null,
        ifEqualsValue: null,
        ifEqualsNull: false,
        ...overrides,
    };
}

function renderRow(field: FieldInfo, onChange = vi.fn()) {
    return render(
        <ChakraProvider value={defaultSystem}>
            <Table.Root>
                <Table.Body>
                    <FieldRow field={field} onChange={onChange} />
                </Table.Body>
            </Table.Root>
        </ChakraProvider>
    );
}

describe("FieldRow", () => {
    it("renders field name", () => {
        renderRow(makeField());
        expect(screen.getByText("name")).toBeInTheDocument();
    });

    it("calls onChange when role changes", async () => {
        const onChange = vi.fn();
        const user = userEvent.setup();

        renderRow(makeField(), onChange);

        const roleSelect = screen.getAllByRole("combobox")[0];
        await user.selectOptions(roleSelect, "PRIORITY_F1");

        expect(onChange).toHaveBeenCalledWith({ role: "PRIORITY_F1" });
    });

    it("shows priority mode select only for priority roles", () => {
        const { rerender } = renderRow(makeField());
        expect(screen.getAllByRole("combobox")).toHaveLength(1);

        rerender(
            <ChakraProvider value={defaultSystem}>
                <Table.Root>
                    <Table.Body>
                        <FieldRow
                            field={makeField({ role: "PRIORITY_F1", priorityMode: "ALWAYS" })}
                            onChange={() => {}}
                        />
                    </Table.Body>
                </Table.Root>
            </ChakraProvider>
        );

        expect(screen.getAllByRole("combobox")).toHaveLength(2);
    });
});