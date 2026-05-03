import Foundation
import FirebaseFirestore

class DatabaseService {
    static let shared = DatabaseService()
    private let db = Firestore.firestore()
    private let defaults = UserDefaults.standard

    func getCards(userId: String) async throws -> [BusinessCard] {
        // Hızlı yükleme için önce yerel veriyi dön
        if let data = defaults.data(forKey: "cards_\(userId)"),
           let localCards = try? JSONDecoder().decode([BusinessCard].self, from: data) {
            return localCards
        }
        return []
    }

    // Bulut ile senkronize et
    func syncCards(userId: String) async throws -> [BusinessCard] {
        let snapshot = try await db.collection("users").document(userId).collection("cards").getDocuments()
        let cards = snapshot.documents.compactMap { doc -> BusinessCard? in
            // BusinessCard modelinizin Firestore'dan decode edilebilir olduğundan emin olun
            // Basitlik için burada manuel eşleme veya JSON üzerinden dönüşüm yapılabilir
            return try? doc.data(as: BusinessCard.self)
        }
        try saveCardsLocally(userId: userId, cards: cards)
        return cards
    }

    func insertCard(card: BusinessCard) async throws {
        var mutableCard = card
        if mutableCard.id == nil {
            mutableCard.id = UUID().uuidString
        }

        // 1. Yerel Kayıt
        var localCards = (try? await getCards(userId: card.userId)) ?? []
        if let index = localCards.firstIndex(where: { $0.id == mutableCard.id }) {
            localCards[index] = mutableCard
        } else {
            localCards.append(mutableCard)
        }
        try saveCardsLocally(userId: card.userId, cards: localCards)

        // 2. Firebase Kayıt
        try await db.collection("users").document(card.userId).collection("cards")
            .document(mutableCard.id!)
            .setData(from: mutableCard)
    }

    func updateCard(card: BusinessCard) async throws {
        try await insertCard(card: card)
    }

    func deleteCard(userId: String, cardId: String) async throws {
        // 1. Yerel Silme
        var localCards = (try? await getCards(userId: userId)) ?? []
        localCards.removeAll { $0.id == cardId }
        try saveCardsLocally(userId: userId, cards: localCards)

        // 2. Firebase Silme
        try await db.collection("users").document(userId).collection("cards")
            .document(cardId)
            .delete()
    }

    private func saveCardsLocally(userId: String, cards: [BusinessCard]) throws {
        let data = try JSONEncoder().encode(cards)
        defaults.set(data, forKey: "cards_\(userId)")
    }
}
