import SwiftUI

struct CustomTextField: View {
    @Binding var text: String
    var placeholder: String
    var icon: String

    var body: some View {
        HStack {
            Image(systemName: icon).foregroundColor(.gray).frame(width: 20)
            TextField(placeholder, text: $text)
                .padding(15)
        }
        .padding(.leading, 15)
    }
}

struct CustomSecureField: View {
    @Binding var text: String
    var placeholder: String
    var icon: String

    var body: some View {
        HStack {
            Image(systemName: icon).foregroundColor(.gray).frame(width: 20)
            SecureField(placeholder, text: $text)
                .padding(15)
        }
        .padding(.leading, 15)
    }
}

extension Color {
    init(hex: String) {
        let scanner = Scanner(string: hex)
        var rgbValue: UInt64 = 0
        scanner.scanHexInt64(&rgbValue)
        let r = Double((rgbValue & 0xFF0000) >> 16) / 255.0
        let g = Double((rgbValue & 0x00FF00) >> 8) / 255.0
        let b = Double(rgbValue & 0x0000FF) / 255.0
        self.init(red: r, green: g, blue: b)
    }

    static let blueGrey = Color(red: 0.376, green: 0.490, blue: 0.545)
    static let amber = Color(red: 1.0, green: 0.75, blue: 0.0)
}

struct ColorPickerRow: View {
    @Binding var selectedColor: String

    private let colors = [
        "0xFFE3F2FD",
        "0xFFE8F5E9",
        "0xFFFFF3E0",
        "0xFFF3E5F5",
        "0xFFFAFAFA",
        "0xFF2C2C2C",
    ]

    var body: some View {
        HStack(spacing: 12) {
            ForEach(colors, id: \.self) { color in
                Circle()
                    .fill(Color(hex: color.replacingOccurrences(of: "0xFF", with: "")))
                    .frame(width: 32, height: 32)
                    .overlay(
                        Circle()
                            .stroke(selectedColor == color ? Color.blueGrey : Color.clear, lineWidth: 3)
                    )
                    .onTapGesture {
                        selectedColor = color
                    }
            }
        }
        .padding(.vertical, 4)
    }
}
