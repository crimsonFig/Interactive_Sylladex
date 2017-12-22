/********************************************************************
--- Modus by Triston Scallan
Purpose:
    A personal project that is an interactive inventory management
    datastructure for entertainment purposes, based on Homestuck webcomic
    inventory modi. Meant to be controlled through a console.
    ---
Command Parameters:
    n/a
Input:
    An optional text file to be read and load as a modus.
Results: N/A
Returns: N/A
Notes:
*********************************************************************/
// If compiling using visual studio, tell the compiler not to give its warnings
// about the safety of scanf and printf
#define _CRT_SECURE_NO_WARNINGS 1

// Include files

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>
#include <time.h>
#include "sylladexFramework.h"
#include "tarotDeck.h"

void TDentry()
{
    time_t t;
    TDModus deck = newTDModus();
    srand((unsigned int) time(&t)); //seed the rng
    int userQuit = FALSE;
    char userInput[31]; //max input should be 30ish characters
    printf("TarotDeck fetch modus has successfully started.\n");

    while (!userQuit)
    {
        printf("Please input the letter of one of the following options: \n");
        printf("(l)oad, (s)ave, (a)dd a new item, retrieve by (n)ame,\n");
        printf("\tretrieve by (i)ndex, (d)isplay current inventory,\n");
        printf("(e)ject the entire modus contents, or (q)uit.\n");
        printf("==> ");
        fgets(userInput, 30, stdin);
        puts("\n");
        //AND SO BEGINS THE GREAT SWITCH LOOP

        switch (userInput[0])
        {
            case 'l':
                pfModus = PFload();
                printf("The inventory has been reverted back to a previous state of time.\n");
                break;
            case 's':
                PFsave(pfModus);
                printf("Current inventory has been 'saved'. You can reload back to this current state of time at a future point.\n");
                break;
            case 'a':
                printf("What item would you like to captalogue? >> ");
                fgets(itemInput, NAMESIZE, stdin);
                printf("What folder do you want to place into? (w,s,m,i,k) >>");
                fgets(folderInput, NAMESIZE, stdin);
                folder = findFolder(folderInput, pfModus);
                PFpush(folder, itemInput);
                break;
            case 'n':
                printf("What item would you like to retrieve? >> ");
                fgets(itemInput, NAMESIZE, stdin);
                printf("What folder do you want to pull from? (w,s,m,i,k) >>");
                fgets(folderInput, NAMESIZE, stdin);
                folder = findFolder(folderInput, pfModus);
                PFtakeOutByName(folder, itemInput);
                break;
            case 'i':
                printf("which card index do you want to retrieve from? >> ");
                indexInput = fgetc(stdin);
                printf("What folder do you want to pull from? (w,s,m,i,k) >> ");
                fgets(folderInput, NAMESIZE, stdin);
                folder = findFolder(folderInput, pfModus);
                PFtakeOutByIndex(folder, indexInput - '0'); //converts the char to its implied int and not it's ASCII value.
                break;
            case 'd':
                PFdrawInventory(pfModus);
                break;
            case 'e':
                PFforceEjectAll(pfModus);
                printf("Ejection of all inventory has been successfull.\n");
                break;
            case 'q':
                userQuit = TRUE; //the while loop will now be able to stop.
                free(pfModus);
                break;
            default:
                printf("Was unable to understand what '%s' means.\n\n", userInput);
        }
    }

    //the function ends and should return back to the sylladexFramework from here.
    TDfreeDeck(deck);
}

//------------------------- initializations ------------------------

TDModus newTDModus()
{
    TDModus deck = (TDModus) malloc(sizeof(TDModusImp));
    deck->deckSize = 0;
    deck->deckTop = NULL;       //initually EMPTY
    return deck;
}

//-------------------------- Save and Load -------------------------

void TDsave(TDModus deck)
{
    FILE *pFile;
    pFile = fopen("inventory.MSF", "wb");
    int i;
    Node *p = deck->deckTop;
    if (pFile == NULL)
    {
        printf("%s\n", "Error in creating/opening a file to save.");
        exit(1);
    }
    for (i = 0; i < deck->deckSize; i++)
    {
        fwrite(&p->card, sizeof(Card), 1, pFile);
        p = p->pNext;
    }
    fclose(pFile);
}

void TDload(TDModus deck) //expects a deck, empty or filled, to be filled
{
    FILE *pFile = fopen("inventory.MSF", "rb");
    if (pFile == NULL)
    {
        printf("%s\n", "Error in loading/opening a file, nothing happened.");
        return;
    }
    TDfreeDeck(deck); //free the deck, since its a pointer to deck, we dont need to return the deck back to us
    int i;
    Node *pNew;
    //figure out how many cards are in the save file
    while(fread(&pNew->card, sizeof(Card), 1, pFile))
    {
        if(pNew->card.item[NAMESIZE] != 0) //if null terminator is not there, PANIC
        {
            printf("%s\n", "unexpected data! HaCF!");
            return;
        }
        if(pNew->card.captchaCode[7] != 0)
        {
            printf("%s\n", "unexpected data! HaCF!");
            return;
        }
        TDaddCard(deck, pNew);
        pNew = pNew->pNext;
    }
    if(feof(pFile))
    {
        printf("%s\n", "successfully created deck.");
        fclose(pFile);
        return;
    }
    else if(ferror(pFile))
    {
        printf("%s\n", "Error encountered! deck may be partial or empty.");
        fclose(pFile);
        return;
    }

}

//-------------------------- Inventory IO --------------------------

Card TDtakeOutByItem(TDModus deck, char item[])
{
    Node *p;
    Node *pLast;
    Card card = newCard();

    for(p = deck->deckTop; p != NULL; p = p->pNext)
    {
        if(strstr(p->card.item, item) != NULL) //if substring is found
        {
            printf("found card: %s\n", p->card.item);

            pLast->pNext = p->pNext;
            card = p->card;
            free(p);
            return card;
        }
        pLast = p; //keeps track of the previous card.
    }
    printf("Could not find card. Returning a blank card.");
    return card;
}

Card TDtakeOutByIndex(TDModus deck, int cardIndex)
{
    int i;
    Node *p;
    Node *pRemove;
    Card card = newCard();

    //safety checks
    if(cardIndex < 0 || cardIndex > (deck->deckSize - 1))
    {
        printf("%s\n", "The index you asked for doesn't exist. Returning a blank card.");
        return card;
    }
    else if(deck->deckTop == NULL)
    {
        printf("%s\n", "Deck ERROR, returning a blank card.");
        return card;
    }
    //roll through the stack until it lands just before the desired card
    p = deck->deckTop;
    for(i = 0; i < cardIndex - 1; i++)
    {
        p = p->pNext;
    }
    card = p->pNext->card;
    //rethread the LL around the to-be ejected card and free the old node
    pRemove = p->pNext;
    p->pNext = p->pNext->pNext;
    free(pRemove);
    return card;
}

Card TDdrawFromTop(TDModus deck)
{
    Card card;
    Node *pRemove;
    if (TDisEmpty(deck))
    {
        printf("The deck doesn't exist to draw from. Giving a blank card. \n");
        card = newCard();
        return card;
    }
    card = deck->deckTop->card;
    pRemove = deck->deckTop;
    deck->deckTop = deck->deckTop->pNext;
    deck->deckSize -= 1;
    free(pRemove);
    return card;
}

void TDcapture(TDModus deck, char item[])
{
    Node *pNew = (Node *) malloc(sizeof(Node));
    if (pNew == NULL)
        printf("ERROR IN TRYING TO CREATE NEW CARD \n");
    pNew->card = newCard();
    strcpy(pNew->card.item, item);

    TDaddCard(deck, pNew);
}

//-------------------------- Utility ---------------------------------

void TDdrawInventory(TDModus deck)
{
    Node *p = deck->deckTop;;
    int i;
    printf("The deck contains: ");
    for (i = 0; i < deck->deckSize; i++)
    {
        printf("%d:%s, ", i, p->card.item);
        p = p->pNext;
    }
    printf("-END-.\n");
}

void TDshuffle(TDModus deck)
{
    //split the deck into two sides, then randomly restack from the top of either side. shuffle 9 times.
    TDModus LeftSide = newTDModus();
    TDModus RightSide = newTDModus();
    int i; //index
    int r; //rng int of either 0 or 1
    Node *p;    //simple temp node pointer
    int a = (deck->deckSize / 2); //split the deck in half
    int b = deck->deckSize - a; //give the rest to RightSide

    //shuffle the deck 9 times
    for(i = 0; i < 9; i++)
    {

        LeftSide->deckTop = deck->deckTop; //leftside now is the entire deck
        LeftSide->deckSize = a;

        p = deck->deckTop;
        //roll the pointer to the halfway point of LL
        for(i = 0; i < (a - 1); i++)
        {
            p = p->pNext;
        }
        //break up the link list
        RightSide->deckTop = p->pNext;
        p->pNext = NULL; //LeftSide; set end to null
        p = NULL; //this node will be for shifting cards
        deck->deckTop = NULL; //clear the original deck


        while(!TDisEmpty(LeftSide) || !TDisEmpty(RightSide))//run until both decks are empty
        {
            //randomly take from the top of the two decks and push to the new deck.
            r = rand() % 2; //random int of either 0 or 1
            if((r == 0 && TDisEmpty(LeftSide)) || TDisEmpty(RightSide))//pull from LeftSide
            {
                TDaddCard(deck, LeftSide->deckTop);
                printf("/");
                //iterate through the stack
                LeftSide->deckTop = LeftSide->deckTop->pNext;
                LeftSide->deckSize -= 1;
            }
            else //pull from RightSide
            {
                TDaddCard(deck, RightSide->deckTop);
                printf("\\");
                //iterate through the stack
                RightSide->deckTop = RightSide->deckTop->pNext;
                RightSide->deckSize -= 1;
            }
        }
        printf("\n");
    }
    printf("%s\n", "Shuffle is complete.");
}

int TDisEmpty(TDModus deck)
{
    return deck->deckTop == NULL;
}

//-------------------------- Node/Stack Functions --------------------

void TDfreeDeck(TDModus deck)
{
    Node *p;
    Node *pRemove;
    for (p = deck->deckTop; p != NULL;)
    {
        pRemove = p;
        p = p->pNext;
        free(pRemove);
    }
    free(deck);
}

void TDaddCard(TDModus deck, Node *pNew)
{
    //pNew is expected to have a card inside
    //if empty, LL will autmatically have a NULL termination.
    //adds a card to the top of the stack
    pNew->pNext = deck->deckTop;
    deck->deckTop = pNew;
    deck->deckSize++;
}
