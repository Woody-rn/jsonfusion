import { Button, Dialog, Portal } from "@chakra-ui/react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { deleteSession } from "../../api/sessions";
import { useFieldsStore } from "../../store/fieldsStore";
import { useSessionStore } from "../../store/sessionStore";
import { useUnmatchedStore } from "../../store/unmatchedStore";

export default function StartOverButton() {
    const navigate = useNavigate();
    const sessionId = useSessionStore((s) => s.sessionId);
    const clearSession = useSessionStore((s) => s.clear);
    const clearFields = useFieldsStore((s) => s.clear);
    const clearUnmatched = useUnmatchedStore((s) => s.clear);

    const [open, setOpen] = useState(false);

    const handleConfirm = async () => {
        if (sessionId) {
            await deleteSession(sessionId).catch(() => undefined);
        }
        clearSession();
        clearFields();
        clearUnmatched();
        setOpen(false);
        navigate("/upload");
    };

    return (
        <>
            <Button size="sm" variant="ghost" onClick={() => setOpen(true)}>
                Start over
            </Button>

            <Dialog.Root open={open} onOpenChange={(e) => setOpen(e.open)}>
                <Portal>
                    <Dialog.Backdrop />
                    <Dialog.Positioner>
                        <Dialog.Content>
                            <Dialog.Header>
                                <Dialog.Title>Начать заново?</Dialog.Title>
                            </Dialog.Header>
                            <Dialog.Body>
                                Текущая сессия будет удалена, все настройки и результаты
                                потеряны.
                            </Dialog.Body>
                            <Dialog.Footer>
                                <Button variant="ghost" onClick={() => setOpen(false)}>
                                    Отмена
                                </Button>
                                <Button colorPalette="red" onClick={handleConfirm}>
                                    Удалить
                                </Button>
                            </Dialog.Footer>
                        </Dialog.Content>
                    </Dialog.Positioner>
                </Portal>
            </Dialog.Root>
        </>
    );
}