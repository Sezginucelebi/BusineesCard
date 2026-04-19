import Foundation
import CoreNFC

class NfcService: NSObject, NFCNDEFReaderSessionDelegate {
    private var session: NFCNDEFReaderSession?
    private var vCardData: String = ""

    func writeCardToTag(card: BusinessCard) {
        vCardData = "BEGIN:VCARD\nVERSION:3.0\nN:\(card.name)\nORG:\(card.company)\nTITLE:\(card.title)\nTEL:\(card.phones)\nEMAIL:\(card.email)\nADR:\(card.address)\nURL:\(card.website)\nEND:VCARD"

        guard NFCNDEFReaderSession.readingAvailable else { return }

        session = NFCNDEFReaderSession(delegate: self, queue: nil, invalidateAfterFirstRead: false)
        session?.alertMessage = "Kartı NFC etiketine yazmak için telefonu etikete yaklaştırın."
        session?.begin()
    }

    func readerSession(_ session: NFCNDEFReaderSession, didDetectNDEFs messages: [NFCNDEFMessage]) {
        // Okuma işlemi (opsiyonel)
    }

    func readerSession(_ session: NFCNDEFReaderSession, didDetect tags: [NFCNDEFTag]) {
        guard tags.count == 1 else {
            session.invalidate(errorMessage: "Birden fazla etiket tespit edildi.")
            return
        }

        let tag = tags.first!

        session.connect(to: tag) { error in
            if error != nil {
                session.invalidate(errorMessage: "Bağlantı hatası.")
                return
            }

            tag.queryNDEFStatus { status, capacity, error in
                if status == .readWrite {
                    let record = NFCNDEFPayload.init(
                        format: .media,
                        type: "text/vcard".data(using: .utf8)!,
                        identifier: Data(),
                        payload: self.vCardData.data(using: .utf8)!
                    )

                    let message = NFCNDEFMessage(records: [record])

                    tag.writeNDEF(message) { error in
                        if error != nil {
                            session.invalidate(errorMessage: "Yazma başarısız.")
                        } else {
                            session.alertMessage = "Kart başarıyla NFC etiketine yazıldı."
                            session.invalidate()
                        }
                    }
                } else {
                    session.invalidate(errorMessage: "Etiket yazılabilir değil.")
                }
            }
        }
    }

    func readerSession(_ session: NFCNDEFReaderSession, didInvalidateWithError error: Error) {
        // Hata yönetimi
    }
}
