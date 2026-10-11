import { Flex, Spinner } from "@chakra-ui/react";
import { useEffect, useState, type ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { getSession } from "../../api/sessions";
import type { SessionStatus } from "../../api/types";
import { useSessionStore } from "../../store/sessionStore";

interface StepGuardProps {
    children: ReactNode;
    requireStatus: SessionStatus[];
}

export default function StepGuard({ children, requireStatus }: StepGuardProps) {
    const { sessionId, status, setStatus, clear } = useSessionStore();

    const statusOk = status !== null && requireStatus.includes(status);
    const [resolved, setResolved] = useState(statusOk);

    useEffect(() => {
        if (!sessionId) return;
        if (statusOk) {
            setResolved(true);
            return;
        }

        getSession(sessionId)
            .then((s) => setStatus(s.status))
            .catch(() => clear())
            .finally(() => setResolved(true));
    }, [sessionId, statusOk, setStatus, clear]);

    if (!sessionId) {
        return <Navigate to="/upload" replace />;
    }

    if (!resolved) {
        return (
            <Flex justify="center" py="16">
                <Spinner size="lg" />
            </Flex>
        );
    }

    if (!statusOk) {
        return <Navigate to="/upload" replace />;
    }

    return <>{children}</>;
}