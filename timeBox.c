/******************************************************************************
--- Modus by Triston Scallan
Purpose:
    A personal project that is an interactive inventory management
    datastructure for entertainment and education purposes, based on
    Homestuck webcomic inventory modi. Meant to be controlled through
    a console.
    --Time Box--
    The time box is a safe-like container with an inside that has no anchor to the current time, allowing items placed inside to shift back and forth through time. From the user's perception, opening the safe causes the inside to present whatever item now exists at that time. items may appear before they were placed in, or appear a long time after it was placed within, if the safe is opened and nothing is inside then it is at a state when all items were removed and nothing was ever put in.

    ---
Command Parameters:
    n/a
Input:
    An optional text file to be read and load as a modus.
Results: N/A
Returns: N/A
Notes:
    Due to the nature of the timeBox existing seperate to timelines, loading
        with this method will be considered as opening the safe, putting a
        card into the safe, and then closing the safe, repeated until all
        cards from the save file are exhausted (with each item querying for a
        size of a given item). Saving will strip the size and temporal data
        off of the items, thus loading directly after saving is considered as
        using a new safe to replace the old safe.
    any item placed in will have an absolute timestamp of when it was placed
        within, and a relative timestamp to allow for shifts within it's
        inner dimensional time. every time the door is opened, all entities
        will have their relative timestamp changed as they have now been
        observed and must decide at what times does it exist in relation to
        the real world (if it lands on a time before the current one, then it
        exists in a closed or opened safe of a parrelel world). if the
        current time overlaps the relative time + absolute timestamp of a
        given item, it may appear within the box for the user to interact
        with. several items may appear in the box at once, but each item are
        considered uniquely different if they have a different absolute time.
        items placed together at the same time are considered as one entity
        now, but a given entity may be several items. Each item would still
        have their own card, but an entity would exist as a linked list of
        cards. an entity may be a single card of a linked list, or several
        cards chained together, in an acycled fashion. opening the safe also
        breaks the entity up into single cards, breaking up any multitude of
        seperate entities. Closing the safe updates the current items that
        was inside at the moment of closing so that their absolute time
        reflects the new current time (and thus combining those item into a
        single entitity within time). This creates a side effect of items
        converging temporally together until all items always appear together
        simultaneously.  an item's relative timestamp will appear as a span
        of an hour, so an overlap (and the subsequent given access) is when
        the relative timeshift/timestamp coincides within an hour of the
        current time.
    The only limit to how many items can be placed within the box is based
        only on the "largeness" of an item. you can only close the box if the
        current items within the box does not exceed the safe's spacial
        threshold (if it all fits in the safe, its good). An anomoly can
        occur if several items overlap at a single point in time, where
        opening the box results in the accessable items to exceed the safe's
        threshold; in this case, two random items will converge into a single
        item and being reduced to the size of the largest of the two. this
        convergence will continue until all accessable items are within the
        safe's threshold. The largeness of items will be tagged in the nodes,
        with the nodes always belonging to a given entity.
*******************************************************************************/
// If compiling using visual studio, tell the compiler not to give its warnings
// about the safety of scanf and printf
#define _CRT_SECURE_NO_WARNINGS 1

// Include files

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>
