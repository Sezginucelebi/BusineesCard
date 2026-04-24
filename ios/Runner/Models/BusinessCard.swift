import Foundation

struct BusinessCard: Identifiable, Codable {
    var id: String?
    let name: String
    let title: String
    let company: String
    let address: String
    let phones: String
    let phones2: String?
    let email: String
    let website: String
    var photoPath: String?
    let cardColor: String
    var templateId: Int? // 1, 2 veya 3
    let fontStyle: String
    let userId: String
}
