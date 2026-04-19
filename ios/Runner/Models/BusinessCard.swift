import Foundation

struct BusinessCard: Identifiable, Codable {
    var id: Int?
    let name: String
    let title: String
    let company: String
    let address: String
    let phones: String
    let email: String
    let website: String
    var photoPath: String?
    let cardColor: String
    let fontStyle: String
    let userId: String
}
