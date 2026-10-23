import {
    Box,
    Button,
    Code,
    Collapsible,
    Flex,
    Heading,
    HStack,
    Link,
    Text,
} from "@chakra-ui/react";
import { useState } from "react";

export default function WelcomeBlock() {
    const [showExample, setShowExample] = useState(false);

    return (
        <Box
            bg="blue.50"
            borderWidth="1px"
            borderColor="blue.200"
            borderRadius="md"
            p="5"
        >
            <Heading size="md" mb="2">
                Сравните и смержите два JSON-файла
            </Heading>

            <Text fontSize="sm" color="gray.700" mb="3">
                Система найдёт совпадения по ключевому полю, покажет несовпавшие
                записи и поможет разобрать их вручную. Каждый файл — до 5 МБ.
            </Text>

            <Flex align="center" gap="2" fontSize="sm" color="gray.600" mb="3">
                <Text>Формат:</Text>
                <Code colorPalette="gray" fontSize="xs">
                    [{`{`} "name": "...", "status": "...", ... {`}`}]
                </Code>
                <Text>— массив плоских объектов</Text>
            </Flex>

            <HStack gap="3">
                <Button
                    size="sm"
                    variant="outline"
                    onClick={() => setShowExample((s) => !s)}
                >
                    {showExample ? "Скрыть пример" : "Показать пример"}
                </Button>
                <Link
                    href="/sample.json"
                    download="sample.json"
                    fontSize="sm"
                    color="blue.600"
                    fontWeight="medium"
                >
                    Скачать пример →
                </Link>
            </HStack>

            <Collapsible.Root open={showExample}>
                <Collapsible.Content>
                    <Box
                        mt="3"
                        p="3"
                        bg="white"
                        borderWidth="1px"
                        borderRadius="md"
                        fontFamily="mono"
                        fontSize="xs"
                        whiteSpace="pre"
                        overflowX="auto"
                    >
                        {`[
  { "name": "ООО Ромашка", "status": "active" },
  { "name": "ИП Иванов",   "status": "inactive" }
]`}
                    </Box>
                </Collapsible.Content>
            </Collapsible.Root>
        </Box>
    );
}