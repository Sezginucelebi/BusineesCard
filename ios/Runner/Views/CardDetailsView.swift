import SwiftUI
import CoreImage.CIFilterBuiltins
import CoreNFC

struct CardDetailsView: View {
    @State var card: BusinessCard
    @State private var showTemplatePicker = false

    let isNfcAvailable = NFCNDEFReaderSession.readingAvailable
    let context = CIContext()
    let filter = CIFilter.qrCodeGenerator()

    var body: some View {
        VStack(spacing: 20) {
            // NFC Status Badge
            HStack {
                Image(systemName: isNfcAvailable ? "wave.3.right.circle.fill" : "wave.3.right.circle")
                Text(isNfcAvailable ? "NFC Paylasim Aktif" : "NFC Kapali")
            }
            .font(.system(size: 14, weight: .semibold))
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(isNfcAvailable ? Color.green.opacity(0.1) : Color.red.opacity(0.1))
            .foregroundColor(isNfcAvailable ? .green : .red)
            .clipShape(Capsule())
            .overlay(
                Capsule()
                    .stroke(isNfcAvailable ? Color.green.opacity(0.5) : Color.red.opacity(0.5), lineWidth: 1)
            )
            .padding(.top, 10)

            // Business Card Mockup
            ZStack(alignment: .bottomTrailing) {
                Image("card_bg_\(card.templateId ?? 1)")
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .frame(height: 230)
                    .clipShape(RoundedRectangle(cornerRadius: 24))
                    .shadow(color: Color.black.opacity(0.15), radius: 10, x: 0, y: 5)

                VStack(alignment: .leading, spacing: 4) {
                    Text(card.name.uppercased())
                        .font(.system(size: 22, weight: .black))
                        .foregroundColor(textColor)
                        .kerning(1)

                    Text(card.title)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(textColor.opacity(0.8))

                    Text(card.company)
                        .font(.system(size: 14))
                        .foregroundColor(textColor.opacity(0.8))

                    Spacer()

                    // Info Section with 65% width constraint
                    VStack(alignment: .leading, spacing: 3) {
                        Text(card.address)
                            .font(.system(size: 9))
                            .lineLimit(2)
                            .frame(maxWidth: UIScreen.main.bounds.width * 0.45, alignment: .leading)

                        Text(card.email)
                            .font(.system(size: 9))

                        Text(card.phones)
                            .font(.system(size: 9))

                        if let p2 = card.phones2, !p2.isEmpty {
                            Text(p2).font(.system(size: 9))
                        }
                    }
                    .foregroundColor(textColor.opacity(0.7))
                    .lineSpacing(2)
                }
                .padding(25)
                .frame(maxWidth: .infinity, alignment: .leading)
                .frame(height: 230)

                // QR Code (99pt - Matches Android 99dp)
                if let qrImage = generateQRCode(from: vCardString) {
                    Image(uiImage: qrImage)
                        .interpolation(.none)
                        .resizable()
                        .aspectRatio(1, contentMode: .fit)
                        .frame(width: 93, height: 93) // Inside 99 frame
                        .padding(3)
                        .background(Color.white)
                        .cornerRadius(12)
                        .shadow(color: Color.black.opacity(0.1), radius: 4)
                        .padding(20)
                }
            }
            .padding(.horizontal)

            Spacer().frame(height: 30)

            // Share Button
            Button(action: shareAsImage) {
                HStack(spacing: 12) {
                    Image(systemName: "square.and.arrow.up.fill")
                    Text("KARTVIZITI PAYLAS").font(.system(size: 16, weight: .bold))
                }
                .frame(maxWidth: .infinity)
                .frame(height: 56)
                .background(Color(hex: "2C3E50"))
                .foregroundColor(.white)
                .cornerRadius(18)
                .shadow(color: Color.black.opacity(0.2), radius: 5, y: 3)
            }
            .padding(.horizontal, 25)

            Text("Paylas butonundan resmi paylasabilir veya dijital cuzdaniniza ekleyebilirsiniz.")
                .font(.system(size: 12))
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
                .padding(.top, 8)

            Spacer()
        }
        .navigationTitle("Kart Detayi")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { showTemplatePicker = true }) {
                    Image(systemName: "slider.horizontal.3").foregroundColor(.black)
                }
            }
        }
        .sheet(isPresented: $showTemplatePicker) {
            VStack(spacing: 20) {
                Text("Kart Tasarimi Secin").font(.system(size: 18, weight: .bold)).padding(.top)
                HStack(spacing: 15) {
                    ForEach(1...3, id: \.self) { id in
                        VStack(spacing: 10) {
                            Button(action: {
                                var updated = card
                                updated.templateId = id
                                Task {
                                    try? await DatabaseService.shared.updateCard(card: updated)
                                    card = updated
                                    showTemplatePicker = false
                                }
                            }) {
                                Image("card_bg_\(id)")
                                    .resizable()
                                    .aspectRatio(contentMode: .fill)
                                    .frame(width: 100, height: 65)
                                    .cornerRadius(10)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 10)
                                            .stroke(card.templateId == id ? Color(hex: "2C3E50") : Color.clear, lineWidth: 3)
                                    )
                                    .shadow(radius: 2)
                            }
                        }
                    }
                }
                Spacer()
            }
            .presentationDetents([.height(220)])
            .presentationDragIndicator(.visible)
        }
    }

    var textColor: Color {
        card.templateId == 2 ? .white : Color(hex: "2C3E50")
    }

    var vCardString: String {
        var vcard = "BEGIN:VCARD\nVERSION:3.0\nN:\(card.name)\nORG:\(card.company)\nTITLE:\(card.title)\nTEL;TYPE=CELL:\(card.phones)\n"
        if let p2 = card.phones2, !p2.isEmpty {
            vcard += "TEL;TYPE=WORK:\(p2)\n"
        }
        vcard += "EMAIL:\(card.email)\nADR:\(card.address)\nURL:\(card.website)\nEND:VCARD"
        return vcard
    }

    func generateQRCode(from string: String) -> UIImage? {
        filter.message = Data(string.utf8)
        if let outputImage = filter.outputImage,
           let cgimg = context.createCGImage(outputImage, from: outputImage.extent) {
            return UIImage(cgImage: cgimg)
        }
        return nil
    }

    func shareAsImage() {
        let width: CGFloat = 1050
        let height: CGFloat = 600
        let renderer = UIGraphicsImageRenderer(size: CGSize(width: width, height: height))
        let image = renderer.image { ctx in
            let bgImage = UIImage(named: "card_bg_\(card.templateId ?? 1)")
            bgImage?.draw(in: CGRect(x: 0, y: 0, width: width, height: height))

            let shareTextColor = card.templateId == 2 ? UIColor.white : UIColor(red: 44/255, green: 62/255, blue: 80/255, alpha: 1)

            let nameAttrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.boldSystemFont(ofSize: 60),
                .foregroundColor: shareTextColor,
            ]
            card.name.uppercased().draw(at: CGPoint(x: 60, y: 60), withAttributes: nameAttrs)

            let infoAttrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.systemFont(ofSize: 32),
                .foregroundColor: shareTextColor.withAlphaComponent(0.8),
            ]
            card.title.draw(at: CGPoint(x: 60, y: 150), withAttributes: infoAttrs)
            card.company.draw(at: CGPoint(x: 60, y: 200), withAttributes: infoAttrs)

            let smallAttrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.systemFont(ofSize: 26),
                .foregroundColor: shareTextColor.withAlphaComponent(0.7),
            ]
            card.address.draw(at: CGPoint(x: 60, y: 440), withAttributes: smallAttrs)
            card.email.draw(at: CGPoint(x: 60, y: 490), withAttributes: smallAttrs)
            card.phones.draw(at: CGPoint(x: 60, y: 540), withAttributes: smallAttrs)

            // QR Frame (297px - Matches Android 297px)
            if let qr = generateQRCode(from: vCardString) {
                let frameSize: CGFloat = 297
                let qrSize: CGFloat = 252
                let frameRect = CGRect(
                    x: width - frameSize - 40,
                    y: height - frameSize - 40,
                    width: frameSize,
                    height: frameSize
                )

                let path = UIBezierPath(roundedRect: frameRect, cornerRadius: 30)
                UIColor.white.setFill()
                path.fill()

                let offset = (frameSize - qrSize) / 2
                let qrRect = CGRect(
                    x: frameRect.origin.x + offset,
                    y: frameRect.origin.y + offset,
                    width: qrSize,
                    height: qrSize
                )
                qr.draw(in: qrRect)
            }
        }

        let activityVC = UIActivityViewController(activityItems: [image], applicationActivities: nil)
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = windowScene.windows.first?.rootViewController {
            rootVC.present(activityVC, animated: true)
        }
    }
}
