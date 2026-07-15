import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:refiner_app/main.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const channel = MethodChannel('com.example.refiner_app/backend');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (call) async {
          if (call.method == 'refine') {
            return 'Hey, I will reach there by 8. Is that okay?';
          }
          return null;
        });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  testWidgets('shows ready state without Azure configuration', (tester) async {
    await tester.pumpWidget(const MyApp());

    expect(find.text('Ready to refine'), findsOneWidget);
    expect(find.text('Azure is configured automatically.'), findsOneWidget);
    expect(find.text('Refine test message'), findsOneWidget);
    expect(find.text('Azure OpenAI setup'), findsNothing);
  });
}
