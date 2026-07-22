package service;

import model.Advertisement;
import model.Conversation;
import model.Message;
import model.User;
import repository.AdvertisementRepository;
import repository.ConversationRepository;
import repository.MessageRepository;
import repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;

    public ChatService(ConversationRepository conversationRepository, MessageRepository messageRepository,
                       AdvertisementRepository advertisementRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.advertisementRepository = advertisementRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Message sendMessageToAd(Long adId, String content, String username) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("متن پیام نمی‌تواند خالی باشد!");
        }

        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد!"));

        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("آگهی یافت نشد!"));

        if (!"ACTIVE".equals(ad.getStatus())) {
            throw new RuntimeException("امکان شروع گفت‌وگو برای آگهی‌های غیرفعال وجود ندارد!");
        }

        if (ad.getSeller().getId().equals(sender.getId())) {
            throw new RuntimeException("شما نمی‌توانید به آگهی خودتان پیام بدهید!");
        }

        if ("BLOCKED".equals(sender.getStatus()) || "BLOCKED".equals(ad.getSeller().getStatus())) {
            throw new RuntimeException("امکان ارسال پیام وجود ندارد زیرا یکی از طرفین مسدود شده است.");
        }

        Conversation conversation = conversationRepository.findByBuyerAndAdvertisement(sender, ad)
                .orElseGet(() -> {
                    Conversation newConv = new Conversation();
                    newConv.setBuyer(sender);
                    newConv.setSeller(ad.getSeller());
                    newConv.setAdvertisement(ad);
                    return conversationRepository.save(newConv);
                });

        return createAndSaveMessage(conversation, sender, content.trim());
    }

    @Transactional
    public Message replyToConversation(Long conversationId, String content, String username) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("متن پیام نمی‌تواند خالی باشد!");
        }

        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد!"));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("گفت‌وگو یافت نشد!"));

        if (!conversation.getBuyer().getId().equals(sender.getId()) &&
                !conversation.getSeller().getId().equals(sender.getId())) {
            throw new RuntimeException("خطای امنیتی: شما مجاز به ارسال پیام در این گفت‌وگو نیستید!");
        }

        User otherUser = conversation.getBuyer().getId().equals(sender.getId()) ?
                conversation.getSeller() : conversation.getBuyer();

        if ("BLOCKED".equals(sender.getStatus()) || "BLOCKED".equals(otherUser.getStatus())) {
            throw new RuntimeException("امکان ارسال پیام وجود ندارد زیرا یکی از طرفین مسدود شده است.");
        }

        return createAndSaveMessage(conversation, sender, content.trim());
    }

    private Message createAndSaveMessage(Conversation conversation, User sender, String content) {
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());
        message.setRead(false);

        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return messageRepository.save(message);
    }

    public List<Conversation> getUserConversations(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد!"));

        return conversationRepository.findByBuyerIdOrSellerIdOrderByUpdatedAtDesc(user.getId(), user.getId());
    }

    @Transactional
    public List<Message> getConversationMessages(Long conversationId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد!"));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("گفت‌وگو یافت نشد!"));

        if (!conversation.getBuyer().getId().equals(user.getId()) &&
                !conversation.getSeller().getId().equals(user.getId())) {
            throw new RuntimeException("خطای امنیتی: شما مجاز به مشاهده این گفت‌وگو نیستید!");
        }

        List<Message> messages = messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);

        boolean hasUnread = false;
        for (Message msg : messages) {
            if (!msg.getSender().getId().equals(user.getId()) && !msg.isRead()) {
                msg.setRead(true);
                hasUnread = true;
            }
        }

        if (hasUnread) {
            messageRepository.saveAll(messages);
        }

        return messages;
    }
}