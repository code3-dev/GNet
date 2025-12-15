# GNet Proxy

<div align="center">
  <img src="app/src/main/res/drawable/logo.png" alt="GNet Proxy Logo" width="40%">
  
  <h1>GNet Proxy</h1>
  
  <p><strong>A powerful Android application that provides both HTTP and SOCKS5 proxy capabilities with a modern Material 3 design</strong></p>
  
  <p>Built with Kotlin and Jetpack Compose, it offers users a seamless way to route their network traffic through customizable proxy servers.</p>
  
  [![GitHub release (latest)](https://img.shields.io/github/v/release/code3-dev/GNet)](https://github.com/code3-dev/GNet/releases)
  [![Downloads](https://img.shields.io/github/downloads/code3-dev/GNet/total)](https://github.com/code3-dev/GNet/releases)
  [![GitHub](https://img.shields.io/github/license/code3-dev/GNet)](LICENSE)
</div>

## 🌟 Features

### 🔧 Proxy Capabilities
- **Dual Proxy Support**: Switch between HTTP and SOCKS5 proxy modes
- **Customizable Port Configuration**: Set your preferred port (1024-65535)
- **Multi-IP Support**: Select from available IP addresses for proxy binding
- **Foreground Service**: Ensures stable operation in the background
- **Real-time Logging**: Monitor all proxy activities with detailed logs

### 🎨 User Interface
- **Modern Material 3 Design**: Clean and intuitive interface
- **Theming Options**: Light, Dark, and System default themes
- **Customizable Colors**: Choose from multiple primary color options
- **Responsive Layout**: Works seamlessly on all device sizes

### 📱 Core Functionality
- **VPN Integration**: Secure network routing
- **Hotspot Information**: View hotspot details and status
- **Easy Controls**: Simple start/stop buttons for proxy management
- **Persistent Settings**: Saves your preferences between sessions

## 🚀 Getting Started

### Installation

#### Option 1: Download from Releases
1. Visit the [Releases page](https://github.com/code3-dev/GNet/releases)
2. Download the appropriate APK for your device:
   - **Universal APK**: Compatible with all architectures (larger file size)
   - **ARM64-v8a APK**: Optimized for 64-bit ARM devices
   - **ARMv7a APK**: Optimized for 32-bit ARM devices
3. Enable "Install from unknown sources" in your device settings
4. Open and install the downloaded APK

#### Option 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/code3-dev/GNet.git

# Navigate to the project directory
cd GNet

# Build the APK
./gradlew assembleRelease
```

### Usage
1. Launch the GNet Proxy app
2. Configure your proxy settings in the Settings tab:
   - Select proxy type (HTTP/SOCKS5)
   - Set desired port number
   - Save settings
3. Return to the Home screen
4. Select your preferred IP address
5. Tap "Start Proxy" to begin routing traffic
6. Monitor activity in the Logs tab

## 🔐 Permissions
GNet requires the following permissions to function properly:
- `INTERNET`: To establish network connections
- `ACCESS_NETWORK_STATE`: To monitor network status
- `ACCESS_WIFI_STATE`: To access Wi-Fi information
- `CHANGE_WIFI_STATE`: To manage Wi-Fi hotspot
- `FOREGROUND_SERVICE`: To run the proxy service in the foreground

## 🛠️ Technical Details

### Architecture
- **Language**: Kotlin
- **Framework**: Jetpack Compose
- **Dependency Injection**: Hilt
- **Navigation**: Jetpack Navigation Compose
- **Threading**: Kotlin Coroutines

### Key Components
- `ProxyServerService`: Handles both HTTP and SOCKS5 proxy protocols
- `HomeScreen`: Main dashboard with status indicators
- `SettingsScreen`: Configuration interface for proxy settings
- `LogsScreen`: Real-time logging of proxy activities
- `HotspotScreen`: Displays hotspot information

## 👤 Author

**Hossein Pira**

- Telegram: [@h3dev](https://t.me/h3dev)
- Email: [h3dev.pira@gmail.com](mailto:h3dev.pira@gmail.com)
- Instagram: [@h3dev.pira](https://instagram.com/h3dev.pira)
- X (Twitter): [@albert_com32388](https://x.com/albert_com32388)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.
## 🙏 Acknowledgments

- Thanks to all contributors who have helped shape GNet Proxy
- Inspired by the need for accessible and customizable proxy solutions on Android

---
Made with ❤️ by [Hossein Pira](https://github.com/code3-dev)