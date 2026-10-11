import { Navigate } from "react-router-dom";
import type { ReactNode } from "react";
import { useSessionStore } from "../../store/sessionStore";
import type { SessionStatus } from "../../api/types";

interface StepGuardProps {
    children: ReactNode;
    requireStatus?: SessionStatus[];
}

export default function StepGuard({ children, requireStatus }: StepGuardProps) {
    const { sessionId, status } = useSessionStore();

    if (!sessionId) {
        return <Navigate to="/upload" replace />;
    }

    if (requireStatus && status && !requireStatus.includes(status)) {
        return <Navigate to="/upload" replace />;
    }

    return <>{children}</>;
}