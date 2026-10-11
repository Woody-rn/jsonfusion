import { useEffect } from "react";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { getSession } from "./api/sessions";
import WizardLayout from "./components/layout/WizardLayout";
import FieldsConfigPage from "./pages/FieldsConfigPage";
import ResultPage from "./pages/ResultPage";
import UnmatchedPage from "./pages/UnmatchedPage";
import UploadPage from "./pages/UploadPage";
import { useSessionStore } from "./store/sessionStore";

function App() {
    const { sessionId, setStatus, clear } = useSessionStore();

    useEffect(() => {
        if (!sessionId) return;

        getSession(sessionId)
            .then((session) => setStatus(session.status))
            .catch(() => clear());
    }, [sessionId, setStatus, clear]);

    return (
        <BrowserRouter>
            <WizardLayout>
                <Routes>
                    <Route path="/" element={<Navigate to="/upload" replace />} />
                    <Route path="/upload" element={<UploadPage />} />
                    <Route path="/fields" element={<FieldsConfigPage />} />
                    <Route path="/unmatched" element={<UnmatchedPage />} />
                    <Route path="/result" element={<ResultPage />} />
                    <Route path="*" element={<div>404 — Not Found</div>} />
                </Routes>
            </WizardLayout>
        </BrowserRouter>
    );
}

export default App;