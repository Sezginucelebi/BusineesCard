import Foundation
// Bu servis, hem iOS hem de Android (Skip transpile) tarafında yerel veritabanı mantığını yönetir.
// Gerçek projede SQLite.swift veya benzeri bir kütüphane kullanılır.

class DatabaseService {
    static let shared = DatabaseService()

    private init() {}

    func getCards(for userId: String) async throws -> [BusinessCard] {
        // Simüle edilmiş veritabanı okuma
        let savedCardsData = UserDefaults.standard.data(forKey: "cards_\(userId)") ?? Data()
        if let decodedCards = try? JSONDecoder().decode([BusinessCard].self, from: savedCardsData) {
            return decodedCards
        }
        return []
    }

    func insertCard(card: BusinessCard) async throws {
        var cards = try await getCards(for: card.userId)
        var newCard = card
        newCard.id = String(Int.random(in: 1000...9999)) // Basit ID üretimi
        cards.append(newCard)
        try saveCards(cards, for: card.userId)
    }

    func updateCard(card: BusinessCard) async throws {
        var cards = try await getCards(for: card.userId)
        if let index = cards.firstIndex(where: { $0.id == card.id }) {
            cards[index] = card
            try saveCards(cards, for: card.userId)
        }
    }

    func deleteCard(id: String, userId: String) async throws {
        var cards = try await getCards(for: userId)
        cards.removeAll(where: { $0.id == id })
        try saveCards(cards, for: userId)
    }

    private func saveCards(_ cards: [BusinessCard], for userId: String) throws {
        let encoded = try JSONEncoder().encode(cards)
        UserDefaults.standard.set(encoded, forKey: "cards_\(userId)")
    }
}
