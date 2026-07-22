package controller;

import model.Conversation;
import model.Message;
import service.ChatService;
import security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    private String extractUsername(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new SecurityException("توکن ارسال نشده یا نامعتبر است!");
        }
        String token = header.substring(7);
        if (!JwtUtil.isTokenValid(token)) {
            throw new SecurityException("توکن منقضی یا نامعتبر است!");
        }
        return JwtUtil.extractUsername(token);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(Exception e, HttpStatus status) {
        return ResponseEntity.status(status).body(Map.of(
                "message", e.getMessage(),
                "status", status.value()
        ));
    }

    private HttpStatus determineHttpStatus(Exception e) {
        String msg = e.getMessage();
        if (msg != null) {
            if (msg.contains("یافت نشد")) {
                return HttpStatus.NOT_FOUND;
            } else if (msg.contains("مجاز") || msg.contains("مسدود") || msg.contains("امنیتی")) {
                return HttpStatus.FORBIDDEN;
            }
        }
        return HttpStatus.BAD_REQUEST;
    }

    @PostMapping("/send-to-ad/{adId}")
    public ResponseEntity<?> sendMessageToAd(
            @PathVariable Long adId,
            @RequestBody(required = false) Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            String content = (request != null) ? request.get("content") : null;
            Message message = chatService.sendMessageToAd(adId, content, username);
            return ResponseEntity.ok(message);
        } catch (SecurityException e) {
            return buildErrorResponse(e, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return buildErrorResponse(e, determineHttpStatus(e));
        }
    }

    @PostMapping("/reply/{conversationId}")
    public ResponseEntity<?> replyToConversation(
            @PathVariable Long conversationId,
            @RequestBody(required = false) Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            String content = (request != null) ? request.get("content") : null;
            Message message = chatService.replyToConversation(conversationId, content, username);
            return ResponseEntity.ok(message);
        } catch (SecurityException e) {
            return buildErrorResponse(e, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return buildErrorResponse(e, determineHttpStatus(e));
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getUserConversations(@RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            List<Conversation> conversations = chatService.getUserConversations(username);
            return ResponseEntity.ok(conversations);
        } catch (SecurityException e) {
            return buildErrorResponse(e, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return buildErrorResponse(e, determineHttpStatus(e));
        }
    }

    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<?> getConversationMessages(
            @PathVariable Long conversationId,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            List<Message> messages = chatService.getConversationMessages(conversationId, username);
            return ResponseEntity.ok(messages);
        } catch (SecurityException e) {
            return buildErrorResponse(e, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return buildErrorResponse(e, determineHttpStatus(e));
        }
    }
}