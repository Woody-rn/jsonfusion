import { Box, Container, Flex, Heading, Steps } from "@chakra-ui/react";
import type { ReactNode } from "react";
import { useLocation, useNavigate } from "react-router-dom";

const STEPS = [
    { path: "/upload", label: "Upload" },
    { path: "/fields", label: "Fields" },
    { path: "/unmatched", label: "Unmatched" },
    { path: "/result", label: "Result" },
];

interface WizardLayoutProps {
    children: ReactNode;
}

export default function WizardLayout({ children }: WizardLayoutProps) {
    const location = useLocation();
    const navigate = useNavigate();

    const currentStep = STEPS.findIndex((s) => location.pathname.startsWith(s.path));

    return (
        <Box minH="100vh" bg="gray.50">
            <Box bg="white" borderBottomWidth="1px" py="4">
                <Container maxW="6xl">
                    <Flex justify="space-between" align="center" mb="4">
                        <Heading size="lg">jsonfusion</Heading>
                    </Flex>

                    <Steps.Root
                        step={currentStep >= 0 ? currentStep : 0}
                        count={STEPS.length}
                        size="sm"
                    >
                        <Steps.List>
                            {STEPS.map((step, index) => (
                                <Steps.Item key={step.path} index={index}>
                                    <Steps.Trigger
                                        onClick={() => navigate(step.path)}
                                        disabled={index > currentStep}
                                    >
                                        <Steps.Indicator />
                                        <Steps.Title>{step.label}</Steps.Title>
                                    </Steps.Trigger>
                                    <Steps.Separator />
                                </Steps.Item>
                            ))}
                        </Steps.List>
                    </Steps.Root>
                </Container>
            </Box>

            <Container maxW="6xl" py="8">
                {children}
            </Container>
        </Box>
    );
}