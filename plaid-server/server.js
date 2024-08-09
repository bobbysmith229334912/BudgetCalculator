// Require necessary packages
const express = require('express');
const bodyParser = require('body-parser');
const plaid = require('plaid');

// Initialize Express app
const app = express();
app.use(bodyParser.json());

// Plaid client configuration
const client = new plaid.PlaidApi(
  new plaid.Configuration({
    basePath: plaid.PlaidEnvironments.sandbox, // Use sandbox for testing
    baseOptions: {
      headers: {
        'PLAID-CLIENT-ID': '6697bf6c6b787e001ad57f23', // Your Plaid client ID
        'PLAID-SECRET': '0227cfd58dbc1cb5eaead1714f68ab', // Your Plaid secret
        'Plaid-Version': '2020-09-14', // Ensure you're using the correct API version
      },
    },
  })
);

// Endpoint to create a link token
app.post('/create_link_token', async (req, res) => {
  // Extract user_id from the request body
  const userId = req.body.user_id; // Assume user_id is sent from the client

  const configs = {
    user: {
      client_user_id: userId, // Use the dynamic user ID received from the client
    },
    client_name: 'Your App Name', // Your application's name
    products: ['auth'], // Products to use, e.g., 'auth', 'transactions'
    country_codes: ['US'], // Country codes
    language: 'en', // Language
  };

  try {
    const response = await client.linkTokenCreate({ link_token_create_request: configs });
    res.json({ link_token: response.data.link_token });
  } catch (error) {
    console.error('Error creating link token:', error.response.data);
    res.status(500).send('Error creating link token');
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
    const itemID = response.data.item_id;

    // Here, you should store accessToken and itemID in your database
    res.json({ access_token: accessToken, item_id: itemID });
  } catch (error) {
    console.error('Error exchanging public token:', error.response.data);
    res.status(500).send('Error exchanging public token');
  }
});

// Start the server
const PORT = process.env.PORT || 8000;
app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
