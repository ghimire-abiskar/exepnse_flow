import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
} from "@modelcontextprotocol/sdk/types.js";
import fetch from "node-fetch";

const server = new Server(
  { name: "android-mcp-bridge", version: "1.1.0" },
  { capabilities: { tools: {} } }
);

const ANDROID_HTTP_URL = "http://localhost:8080";

server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: "get_screen_state",
        description: "Fetches the live UI accessibility tree (XML) of the connected Android device.",
        inputSchema: { type: "object", properties: {} },
      },
      {
        name: "take_screenshot",
        description: "Captures and returns an inline screenshot of the current screen.",
        inputSchema: { type: "object", properties: {} },
      },
      {
        name: "launch_app",
        description: "Launches any installed Android app by its package name (e.g., com.android.chrome).",
        inputSchema: {
          type: "object",
          properties: { packageName: { type: "string", description: "Target app package name" } },
          required: ["packageName"],
        },
      },
      {
        name: "click_element",
        description: "Clicks an element using its resource-id, text, or raw fallback coordinates (x, y).",
        inputSchema: {
          type: "object",
          properties: {
            resourceId: { type: "string", description: "Android resource-id (e.g., com.app:id/btn)" },
            text: { type: "string", description: "Visible button text label" },
            x: { type: "number", description: "X coordinate fallback" },
            y: { type: "number", description: "Y coordinate fallback" },
          },
        },
      },
      {
        name: "long_click_element",
        description: "Performs a long press on an element by resource-id, text, or coordinates.",
        inputSchema: {
          type: "object",
          properties: {
            resourceId: { type: "string" },
            text: { type: "string" },
            x: { type: "number" },
            y: { type: "number" },
          },
        },
      },
      {
        name: "type_text",
        description: "Types text into a focused field or a field found by resource-id.",
        inputSchema: {
          type: "object",
          properties: {
            text: { type: "string", description: "Text content to type" },
            resourceId: { type: "string", description: "Optional resource-id of the input field" },
          },
          required: ["text"],
        },
      },
      {
        name: "swipe_screen",
        description: "Performs a swipe or drag gesture from start coordinates to end coordinates.",
        inputSchema: {
          type: "object",
          properties: {
            startX: { type: "number" }, startY: { type: "number" },
            endX: { type: "number" }, endY: { type: "number" },
            steps: { type: "number", description: "Speed/steps count (default 50)" },
          },
          required: ["startX", "startY", "endX", "endY"],
        },
      },
      {
        name: "scroll_screen",
        description: "Scrolls the nearest scrollable container in a specified direction.",
        inputSchema: {
          type: "object",
          properties: {
            direction: { type: "string", enum: ["down", "up", "left", "right"], description: "Scroll direction" },
          },
          required: ["direction"],
        },
      },
    ],
  };
});

server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  try {
    let endpoint = "";
    let method = "POST";
    let bodyData = args;

    if (name === "get_screen_state") {
      const res = await fetch(`${ANDROID_HTTP_URL}/state`);
      return { content: [{ type: "text", text: await res.text() }] };
    }
    if (name === "take_screenshot") {
      const res = await fetch(`${ANDROID_HTTP_URL}/screenshot`);
      const buffer = await res.buffer();
      return {
        content: [
          { type: "image", data: buffer.toString("base64"), mimeType: "image/png" },
          { type: "text", text: "Screenshot captured." }
        ],
      };
    }

    if (name === "launch_app") endpoint = "/app/launch";
    else if (name === "click_element") endpoint = "/action/click";
    else if (name === "long_click_element") endpoint = "/action/long_click";
    else if (name === "type_text") endpoint = "/action/type";
    else if (name === "swipe_screen") endpoint = "/action/swipe";
    else if (name === "scroll_screen") endpoint = "/action/scroll";
    else throw new Error(`Unknown tool: ${name}`);

    const res = await fetch(`${ANDROID_HTTP_URL}${endpoint}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(bodyData),
    });
    const resultText = await res.text();

    return { content: [{ type: "text", text: resultText }] };
  } catch (error) {
    return { content: [{ type: "text", text: `Error: ${error.message}` }], isError: true };
  }
});

async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
}

main().catch((error) => process.exit(1));