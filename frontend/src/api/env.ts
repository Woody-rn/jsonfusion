// Базовый URL без /api (для ссылок на скачивание)
export const API_BASE_URL =
    import.meta.env.VITE_API_URL ?? "http://localhost:8080";

// Полный URL для axios (с /api)
export const API_URL = `${API_BASE_URL}/api`;