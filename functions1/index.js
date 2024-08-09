// Import necessary modules
const { onRequest } = require('firebase-functions/v2/https');
const express = require('express');
const bodyParser = require('body-parser');
const plaid = require('plaid');
const cors = require('cors');

// Initialize Express app
const app = express();

// Middleware
app.use(cors({ origin: true })); // Enable CORS for all origins
app.use(bodyParser.json());

// Plaid client configuration
const client = new plaid.PlaidApi(
  new plaid.Configuration({
    basePath: plaid.PlaidEnvironments.sandbox, // Use sandbox for testing; use production for live
    baseOptions: {
      headers: {
        'PLAID-CLIENT-ID': '6697bf6c6b787e001ad57f23', // Replace with your Plaid client ID
        'PLAID-SECRET': '0227cfd58dbc1cb5eaead1714f68ab', // Replace with your Plaid secret
        'Plaid-Version': '2020-09-14', // Use the correct Plaid API version
      },
    },
  })
);

// Endpoint to create a link token
app.post('/create_link_token', async (req, res) => {
  const userId = req.body.user_id; // Assume user_id is sent from the client

  const configs = {
    user: {
      client_user_id: userId, // Use the dynamic user ID received from the client
    },
    client_name: 'Budget Calculator App', // Your application's name
    products: ['auth'], // Products to use, e.g., 'auth', 'transactions'
    country_codes: ['US'], // Country codes
    language: 'en', // Language
  };

  try {
    const response = await client.linkTokenCreate(configs);
    res.json({ link_token: response.data.link_token });
  } catch (error) {
    console.error('Error creating link token:', error.response.data);
    res.status(500).json({
      error: 'Error creating link token',
      details: error.response.data
    });
  }
});

// Endpoint to exchange public token for access token
app.post('/exchange_public_token', async (req, res) => {
  const publicToken = req.body.public_token;

  try {
    const response = await client.itemPublicTokenExchange({
      public_token: publicToken,
    });

    const accessToken = response.data.access_token;
    const itemId = response.data.item_id;

    // Here, you should store accessToken and itemId in your database
    res.json({ access_token: accessToken, item_id: itemId });
  } catch (error) {
    console.error('Error exchanging public token:', error.response.data);
    res.status(500).json({
      error: 'Error exchanging public token',
      details: error.response.data
    });
  }
});

// Export the app as a Firebase Cloud Function
exports.api = onRequest(app);
