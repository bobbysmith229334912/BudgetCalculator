
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:social_media_app/services/auth_service.dart';

class AuthScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final authService = Provider.of<AuthService>(context);

    return Scaffold(
      appBar: AppBar(
        title: Text('Sign In'),
      ),
      body: Center(
        child: ElevatedButton(
          onPressed: () async {
            await authService.signInAnonymously();
          },
          child: Text('Sign In Anonymously'),
        ),
      ),
    );
  }
}
