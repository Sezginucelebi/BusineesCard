import SwiftUI
import CoreNFC

struct CardDetailsView: View {
    let card: BusinessCard
    @State private var isNfcAvailable = false

    var body: some View {
        ScrollView {
            VStack(spacing: 25) {
                // NFC Status
                HStack {
                    Image(systemName: isNfcAvailable ? "nfc" : "nfc.fill")
                    Text(isNfcAvailable ? "NFC Paylaşım Aktif" : "NFC Kapalı / Yok")
                }
                .font(.system(size: 13, weight: .bold))
                .padding(.vertical, 8)
                .padding(.horizontal, 16)
                .background(isNfcAvailable ? Color.green.opacity(0.1) : Color.red.opacity(0.1))
                .foregroundColor(isNfcAvailable ? .green : .red)
                .cornerRadius(20)
                .overlay(RoundedRectangle(cornerRadius: 20).stroke(isNfcAvailable ? Color.green : Color.red, lineWidth: 1.5))

                // Card Preview
                ZStack {
                    RoundedRectangle(cornerRadius: 24)
                        .fill(Color(hex: card.cardColor.replacingOccurrences(of: "0xFF", with: "")))
                        .shadow(color: .black.opacity(0.1), radius: 10, x: 0, y: 5)

                    VStack(alignment: .leading) {
                        Text(card.name.uppercased())
                            .font(.system(size: 24, weight: .black))
                            .foregroundColor(Color(hex: "2C3E50"))

                        Text(card.title)
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(.black.opacity(0.7))

                        Text(card.company)
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.black.opacity(0.5))

                        Spacer()

                        HStack(alignment: .bottom) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(card.address).font(.system(size: 11))
                                Text(card.email).font(.system(size: 11))
                                Text(card.phones).font(.system(size: 11, weight: .bold))
                            }
                            .foregroundColor(.black.opacity(0.8))

                            Spacer()

                            // Placeholder for QR Code
                            Image(systemName: "qrcode")
                                .resizable()
                                .frame(width: 60, height: 60)
                                .padding(8)
                                .background(Color.white)
                                .cornerRadius(12)
                        }
                    }
                    .padding(24)
                }
                .frame(height: 220)

                Button(action: shareCard) {
                    Label("KARTVİZİTİ PAYLAŞ", systemImage: "square.and.arrow.up")
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blueGrey)
                        .foregroundColor(.white)
                        .cornerRadius(16)
                }

                Text("Kartınız yüksek kaliteli bir dijital formatta paylaşılacaktır.")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            .padding(20)
        }
        .navigationTitle("Kart Detayı")
        .onAppear {
            isNfcAvailable = NFCNDEFReaderSession.readingAvailable
        }
    }

    func shareCard() {
        // Logic for sharing the card
    }
}
