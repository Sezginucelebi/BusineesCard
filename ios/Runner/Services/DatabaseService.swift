import Foundation

class DatabaseService {
    static let shared = DatabaseService()
    private let defaults = UserDefaults.standard

    func getCards(userId: String) async throws -> [BusinessCard] {
        guard let data = defaults.data(forKey: "cards_\(userId)") else { return [] }
        return try JSONDecoder().decode([BusinessCard].self, from: data)
    }

    func insertCard(card: BusinessCard) async throws {
        var mutableCard = card
        var cards = try await getCards(userId: card.userId)

        if mutableCard.id == nil {
            mutableCard.id = UUID().uuidString
            cards.append(mutableCard)
        } else {
            if let index = cards.firstIndex(where: { $0.id == mutableCard.id }) {
                cards[index] = mutableCard
            }
        }

        try saveCards(userId: card.userId, cards: cards)
    }

    func updateCard(card: BusinessCard) async throws {
        try await insertCard(card: card)
    }

    func deleteCard(userId: String, cardId: String) async throws {
        var cards = try await getCards(userId: userId)
        cards.removeAll { $0.id == cardId }
        try saveCards(userId: userId, cards: cards)
    }

    private func saveCards(userId: String, cards: [BusinessCard]) throws {
        let data = try JSONEncoder().encode(cards)
        defaults.set(data, forKey: "cards_\(userId)")
    }
}
