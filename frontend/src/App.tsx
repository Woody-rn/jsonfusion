import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import WizardLayout from "./components/layout/WizardLayout";
import UploadPage from "./pages/UploadPage";
import FieldsConfigPage from "./pages/FieldsConfigPage";
import UnmatchedPage from "./pages/UnmatchedPage";
import ResultPage from "./pages/ResultPage";

function App() {
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