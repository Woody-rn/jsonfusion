import axios from "axios";

const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8089";

export const http = axios.create({
    baseURL: `${BASE_URL}/api`,
    headers: { "Content-Type": "application/json" },
});