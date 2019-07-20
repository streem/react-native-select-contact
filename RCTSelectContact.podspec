require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name                = "RCTSelectContact"
  s.version             = package['version']
  s.summary             = "Simple iOS contact picker"
  s.homepage            = "https://github.com/streem/react-native-select-contact"
  s.license             = package['license']
  s.author              = package['author']
  s.source              = { :git => 'https://github.com/streem/react-native-select-contact.git', :tag => "v#{s.version}" }
  s.default_subspec     = 'Core'
  s.requires_arc        = true
  s.platform            = :ios, "8.0"
  
  s.dependency 'React'
  
  s.subspec 'Core' do |ss|
    ss.source_files     = "ios/RCTSelectContact/*.{h,m}"
  end

end
