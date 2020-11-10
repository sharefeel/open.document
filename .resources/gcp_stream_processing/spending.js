'use strict';

// [START functions_pubsub_publish]
const {PubSub} = require('@google-cloud/pubsub');

// Instantiates a client
const pubsub = new PubSub();

exports.publish = async (req, res) => {
  if (!req.body) {  
    res .status(400).send('nobody');
    return;
  }

  console.log(`Publishing message to topic projects/stream-ex/topics/spending`);

  const messageObject = JSON.parse(req.body);
  messageObject['collect_time'] = Date.now();
  const messageBuffer = Buffer.from(JSON.stringify(messageObject), 'utf8');

  // References an existing topic

  // Publishes a message
  try {
    if (req.query['type'] == 'cash') {
      const topic = pubsub.topic('projects/stream-ex/topics/spending-cash');      
      await topic.publish(messageBuffer);
      console.log('Message published to cash topic');
      res.status(200).send('Message published to cash topic');      
      
    } else if (req.query['type'] == 'creditCard') {
      const topic = pubsub.topic('projects/stream-ex/topics/spending-cred-card');
      await topic.publish(messageBuffer);
      console.log('Message published to credit card topic');
      res.status(200).send('Message published to credit card topic');
      
    } else {
      console.log('Bad spending type');
      res.status(400).send('Bad spending type');      
    }
  } catch (err) {
      console.error(err);
      res.status(500).send(err);
      return Promise.reject(err);
  }
};
