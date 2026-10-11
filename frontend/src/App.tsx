import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import StepGuard from "./components/layout/StepGuard";
import WizardLayout from "./components/layout/WizardLayout";
import FieldsConfigPage from "./pages/FieldsConfigPage";
import ResultPage from "./pages/ResultPage";
import UnmatchedPage from "./pages/UnmatchedPage";
import UploadPage from "./pages/UploadPage";

function App() {
    return (
        <BrowserRouter>
            <WizardLayout>
                <Routes>
                    <Route path="/" element={<Navigate to="/upload" replace />} />
                    <Route path="/upload" element={<UploadPage />} />
                    <Route
                        path="/fields"
                        element={
                            <StepGuard requireStatus={["FILES_UPLOADED", "CONFIGURED"]}>
                                <FieldsConfigPage />
                            </StepGuard>
                        }
                    />
                    <Route path="/unmatched" element={<UnmatchedPage />} />
                    <Route
                        path="/result"
                        element={
                            <StepGuard requireStatus={["MERGED", "UNMATCHED_RESOLVED", "FINALIZED"]}>
                                <ResultPage />
                            </StepGuard>
                        }
                    />
                    <Route path="*" element={<div>404 — Not Found</div>} />
                </Routes>
            </WizardLayout>
        </BrowserRouter>
    );
}

export default App;