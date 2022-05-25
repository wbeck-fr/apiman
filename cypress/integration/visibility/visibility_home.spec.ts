/*
 * Copyright 2022 Scheer PAS Schweiz AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  imitations under the License.
 */

/// <reference types="cypress" />

import { beforeEach } from 'mocha';

describe('Testing the visibility on home page', () => {
  before(() => {
    cy.cleanUp();
    cy.initApimanData('test-data/visibility.json');
  });

  beforeEach(() => {
    cy.visit('/home');
    cy.tryLogout();
  });

  it('Check APIs anonymous', () => {
    cy.visit('/home');
    cy.get('mat-card.api-card').should('have.length', 2);
  });

  it('Check APIs devportal user', () => {
    cy.visit('/home');
    cy.login(
      Cypress.env('devportalUser') as string,
      Cypress.env('devportalPassword') as string
    );
    cy.get('mat-card.api-card').should('have.length', 2);
  });

  it('Check APIs platform user', () => {
    cy.visit('/home');
    cy.logout();
    cy.login(
      Cypress.env('nonAdminUser') as string,
      Cypress.env('nonAdminPassword') as string
    );

    cy.get('mat-card.api-card').should('have.length', 3);
  });

  it('Check APIs org member', () => {
    cy.visit('/home');
    cy.logout();
    cy.login(
      Cypress.env('adminUser') as string,
      Cypress.env('adminPassword') as string
    );
    cy.get('mat-card.api-card').should('have.length', 4);
  });
});
